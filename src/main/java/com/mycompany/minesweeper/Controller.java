package com.mycompany.minesweeper;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.GridPane;
import java.net.URL;
import java.util.ResourceBundle;
import javafx.scene.control.Label;
import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.input.MouseButton;
import javafx.scene.text.Font;
import javafx.scene.layout.AnchorPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.text.FontWeight;



public class Controller implements Initializable {
    @FXML private GridPane Board;
    @FXML private Label GameOver;
    @FXML private AnchorPane Anchor;
    @FXML private Button Restart;
    @FXML private Label Head;
    @FXML private Label Timer;
    
    private AnimationTimer gameTimer; // <--- New Timer object
    private long lastTime = 0; // To store the time of the previous frame (in nanoseconds)
    private long totalElapsedTime = 0; // To accumulate elapsed time (in nanoseconds)
    private final StringProperty timeDisplay = new SimpleStringProperty("0.00");
    int ROW = 9;
    int COL = 9;
    int MINES = 10;
    MediaPlayer MP;
    MediaView MV;
    int NonMines = ROW*COL-MINES;
    int NumOfRevealed = 0;
    int NumMinesFlagged = 0;
    private final Button[][] buttons = new Button[COL][ROW];
    private final Label[][] labels = new Label[COL][ROW];
    int[][] Mines = new int[MINES][2];
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        NumMinesFlagged = 0;
        GameOver.setVisible(false);
        String path = App.class.getResource("Bomb.mp4").toExternalForm();
        Media M = new Media(path);
        MP = new MediaPlayer(M);
        MV = new MediaView(MP);
        MV.setPreserveRatio(false);
        
        MV.fitWidthProperty().bind(Anchor.widthProperty());
        MV.fitHeightProperty().bind(Anchor.heightProperty());
        AnchorPane.setLeftAnchor(MV, 0.0);
        AnchorPane.setTopAnchor(MV, 0.0);
        MP.setOnEndOfMedia(()->{
                Anchor.getChildren().remove(MV);
                for(int i = 0; i < MINES; i++) {
                    int col = Mines[i][0];
                    int row = Mines[i][1];
                    labels[col][row].setVisible(true);
                }

                GameOver.setVisible(true);
                
                for (int col = 0; col < COL; col++) {
                    for (int row = 0; row < ROW; row++) {
                        buttons[col][row].setDisable(true);
                    }
                }
            }
        );
        Restart.setStyle(
            "-fx-background-color: #505050;"
        );
        Restart.setOnMouseClicked(event->{   
            NumMinesFlagged = 0;
            GameOver.setVisible(false);
            Head.setText("Sweep The Mines!");
            Head.setFont(Font.font("Times New Roman",FontWeight.BOLD,24));
            NumOfRevealed = 0;
            for (int col = 0; col < COL; col++) {
                for (int row = 0; row < ROW; row++) {
                    labels[col][row].setText("0");
                    labels[col][row].setVisible(false);

                    buttons[col][row].setDisable(false);
                    buttons[col][row].setVisible(true);
                    buttons[col][row].setText(""); // Clear any flag ("🚩")
                }
            }

            PopulateLabels(); 

            for(int i = 0; i < COL; i++)
            {
                for(int j = 0; j < ROW; j++)
                {
                    labels[i][j].setFont(Font.font("Times New Roman", 25)); 
                    String text = labels[i][j].getText();

                    if("1".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #00D0FF;"); }
                    else if("2".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #00FF7F;"); }
                    else if("3".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #FF4040;"); }
                    else if("4".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #5050FF;"); }
                    else if("5".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #FFA500;"); }
                    else if("6".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #00FFFF;"); }
                    else if("7".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #FFFFFF;"); }
                    else if("8".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #C0C0C0;"); }
                    else if("*".equals(text)) { labels[i][j].setStyle("-fx-text-fill: #000000;"); }
                }
            }
            stopTimer();
            setupTimer();
        });
        Anchor.setStyle(
                "-fx-background-color: #252526;" 
        );
        Board.setStyle(
            "-fx-background-color: #363636;" +//Grey21 
            "-fx-border-color: #363636;" + 
            "-fx-border-width: 2;" + 
            "-fx-border-style: solid;"
        );
        for(Node node : Board.getChildren()){
            @SuppressWarnings("null")
            int c = GridPane.getColumnIndex(node) == null ? 0 : GridPane.getColumnIndex(node);
            @SuppressWarnings("null")
            int r = GridPane.getRowIndex(node) == null ? 0 : GridPane.getRowIndex(node);
            
            if(node instanceof Button)
            {
                Button button = (Button)node;
                buttons[c][r] = button;
                button.setVisible(true);
                button.setStyle(
"-fx-background-color: #505050;"
                );
                button.setOnMouseClicked(event->ButtonClicked(c,r,event));
            }
            else if(node instanceof Label){
                Label label = (Label)node;
                label.setText("0");
                label.setAlignment(Pos.CENTER);
                label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
                label.setFont(Font.font("Times New Roman",25));
                label.setStyle(
                        "-fx-background-color: #363636;"
                );
                label.setVisible(false);
                labels[c][r] = label;
            }
        }
        PopulateLabels();
        for(int i = 0; i < COL; i++)
        {
            for(int j = 0; j < ROW; j++)
            {
            if("1".equals(labels[i][j].getText()))
                {
                    // Cyan/Bright Blue
                    labels[i][j].setStyle("-fx-text-fill: #00D0FF;");
                }
                else if("2".equals(labels[i][j].getText()))
                {
                    // Bright Spring Green
                    labels[i][j].setStyle("-fx-text-fill: #00FF7F;");
                }
                else if("3".equals(labels[i][j].getText()))
                {
                    // Soft Red
                    labels[i][j].setStyle("-fx-text-fill: #FF4040;");
                }
                else if("4".equals(labels[i][j].getText()))
                {
                    // Lightened Navy Blue
                    labels[i][j].setStyle("-fx-text-fill: #5050FF;");
                }
                else if("5".equals(labels[i][j].getText()))
                {
                    // Bright Orange/Gold
                    labels[i][j].setStyle("-fx-text-fill: #FFA500;");
                }
                else if("6".equals(labels[i][j].getText()))
                {
                    // Bright Teal/Aqua
                    labels[i][j].setStyle("-fx-text-fill: #00FFFF;");
                }
                else if("7".equals(labels[i][j].getText()))
                {
                    // White (for contrast on a dark background)
                    labels[i][j].setStyle("-fx-text-fill: #FFFFFF;");
                }
                else if("8".equals(labels[i][j].getText()))
                {
                    // Light Gray
                    labels[i][j].setStyle("-fx-text-fill: #C0C0C0;");
                }
                else if("*".equals(labels[i][j].getText()))
                {
                    //Black
                    labels[i][j].setStyle("-fx-text-fill: #000000;");
                }
            }
        }
        setupTimer();
    }
    
    private void setupTimer() {
    // Bind the TimerLabel text to the timeDisplay property (same as before)
        Timer.textProperty().bind(timeDisplay);
        Timer.setFont(Font.font("Times New Roman", FontWeight.BOLD, 24));
        Timer.setStyle("-fx-text-fill: #FFFFFF;");
        timeDisplay.set("0:00.00");

        gameTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                if (lastTime > 0) {
                    // Calculate time elapsed since the last frame call in nanoseconds
                    totalElapsedTime += now - lastTime;

                    // Update the UI only if a significant time has passed (e.g., every 10ms)
                    // This ensures smooth updates while avoiding excessive calculations every frame
                    if (totalElapsedTime >= 10_000_000) { // 10 million nanoseconds = 10 milliseconds
                        updateTime(totalElapsedTime);
                    }
                }
                lastTime = now;
            }
        };
    }

    private void startTimer() {
        // Reset accumulated time and start the timer
        totalElapsedTime = 0;
        lastTime = 0; // Setting lastTime to 0 ensures the first frame doesn't calculate massive elapsed time
        timeDisplay.set("0:00.00");
        gameTimer.start();
    }

    private void stopTimer() {
        gameTimer.stop();
    }    
    
    // Original signature was: private void updateTime()
    private void updateTime(long nanoseconds) { // <--- CHANGE SIGNATURE
        // Convert nanoseconds to milliseconds
        long elapsedMillis = nanoseconds / 1_000_000;

        // Extract time components
        long totalSeconds = elapsedMillis / 1000;

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        // Centiseconds: The remaining milliseconds divided by 10 (00-99)
        long centiseconds = (elapsedMillis % 1000) / 10; 

        // Format as Minutes:Seconds.Centiseconds (e.g., 5:03.25)
        String formattedTime = String.format("%d:%02d.%02d", minutes, seconds, centiseconds);

        timeDisplay.set(formattedTime);
    }
    
    @SuppressWarnings("ConvertToStringSwitch")
    private void ButtonClicked(int c, int r, javafx.scene.input.MouseEvent event){
        if (event.getButton() == MouseButton.PRIMARY) {    
            if("🚩".equals(buttons[c][r].getText()))return;
            if("*".equals(labels[c][r].getText()))
            {
                stopTimer();
                Anchor.getChildren().add(MV); 
                MV.setVisible(true);
                MP.stop();
                MP.seek(javafx.util.Duration.ZERO);
                MP.play();
            }
            else if("".equals(labels[c][r].getText()))
                revealCell(c,r);
            else
            {

                buttons[c][r].setVisible(false);
                labels[c][r].setVisible(true);
                NumOfRevealed+=1;
                if(NumOfRevealed==1 && NumMinesFlagged==0)startTimer();
                if(VisitedAll()){
                    Congrats();
                }
            }
        }
        else
        {    
            if("".equals(buttons[c][r].getText())){
                if("*".equals(labels[c][r].getText()))NumMinesFlagged++;
                buttons[c][r].setText("🚩");
                buttons[c][r].setFont(Font.font("Times New Roman",16));
                buttons[c][r].setStyle(
                    "-fx-text-fill: white;" +
                    "-fx-background-color: #505050;"
                );
                if((NumOfRevealed==0 && NumMinesFlagged==1) || (NumOfRevealed==0 && NumMinesFlagged==0))startTimer();
                if(VisitedAll()){
                    Congrats();
                }
            }
            else if("🚩".equals(buttons[c][r].getText())){
                if("*".equals(labels[c][r].getText()))NumMinesFlagged--;
                buttons[c][r].setText("");
            }
        }
    }

    private void PopulateLabels()
    {
        int placed = 0;
        Random random = new Random();
        while(placed<10)
        {
            int row = random.nextInt(ROW);
            int col = random.nextInt(COL);
            if(!"*".equals(labels[col][row].getText()))
            {
                labels[col][row].setText("*");
                Mines[placed][0] = col;
                Mines[placed][1] = row;
                placed++;
            }
        }
        PlaceNums();
    }
    private void PlaceNums()
    {
        for(int i = 0; i < MINES; i++)
        {
            int c = Mines[i][0];
            int r = Mines[i][1];
            for(int j = c-1; j <= c+1; j++)
            {
                for(int k = r-1; k <= r+1; k++)
                {
                    if (j < 0 || j >= COL || k < 0 || k >= ROW)
                        continue;

                    if (k == r && j == c)
                        continue;

                    if (!"*".equals(labels[j][k].getText()))
                    {
                        int num = Integer.parseInt(labels[j][k].getText());
                        num++;
                        String s = "" + num;
                        labels[j][k].setText(s);
                    }
                }
            }
        }
        
        for(int i = 0; i < COL; i++)
        {
            for(int j = 0; j < ROW; j++)
            {
                if("0".equals(labels[i][j].getText()))
                {
                    labels[i][j].setText("");
                }
            }
        }
    }
    
    private void revealCell(int c, int r) {
        
        if (c < 0 || c >= COL || r < 0 || r >= ROW) {
            return; 
        }
        
        Button button = buttons[c][r];
        
        if (!button.isVisible() || "🚩".equals(button.getText())) {
            return;
        }

        button.setVisible(false);
        NumOfRevealed+=1;
        labels[c][r].setVisible(true);
        if(NumOfRevealed==1 && NumMinesFlagged==0)startTimer();
        
        if (VisitedAll()) {
            Congrats();
            return;
        }
        
        String content = labels[c][r].getText();
        if (!"".equals(content) && !"*".equals(content)) {
            return; 
        }
        
        if ("".equals(content)) {
            for (int i = c - 1; i <= c + 1; i++) {
                for (int j = r - 1; j <= r + 1; j++) {
                    
                    if (i == c && j == r) continue; 
                                        
                    revealCell(i, j);
                }
            }
        }
    }
    
    private boolean VisitedAll(){
        return ((NumOfRevealed==NonMines)||(NumMinesFlagged==MINES));
    }
    
    private void Congrats(){
        stopTimer();
        Head.setText("Congratulations!!");
        Head.setFont(Font.font("Times New Roman", FontWeight.BOLD, 24));
        Head.setVisible(true);    
        for(int i = 0; i < COL; i++)
        {
            for(int j = 0; j < ROW; j++)
            {
                if("*".equals(labels[i][j].getText())){
                    buttons[i][j].setDisable(true);
                    buttons[i][j].setText("🚩");
                    buttons[i][j].setFont(Font.font("Times New Roman",16));
                    buttons[i][j].setStyle(
                        "-fx-text-fill: white;" +
                        "-fx-background-color: #505050;"
                    );
                }
                else if(buttons[i][j].isVisible()){
                    buttons[i][j].setDisable(true);
                    buttons[i][j].setVisible(false);
                    labels[i][j].setVisible(true);
                }
            }
        }
    }
}