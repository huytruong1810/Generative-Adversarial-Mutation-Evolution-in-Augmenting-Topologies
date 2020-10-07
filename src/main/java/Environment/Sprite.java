package Environment;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

public class Sprite {

    Group mainGroup, Up, Right, Down, Left;
    Timeline mainTimeLine, up, right, down, left;
    TranslateTransition mainTransition, tUp, tRight, tDown, tLeft;

    public Sprite(double c, String up0, String up1, String up2, String right0, String right1, String right2,
                  String down0, String down1, String down2, String left0, String left1, String left2) {

        double h = new Image(up0).getHeight()*c;
        double w = new Image(up0).getWidth()*c;
        ImageView Up0 = new ImageView(up0); ImageView Up1 = new ImageView(up1); ImageView Up2 = new ImageView(up2);
        ImageView Right0 = new ImageView(right0); ImageView Right1 = new ImageView(right1); ImageView Right2 = new ImageView(right2);
        ImageView Down0 = new ImageView(down0); ImageView Down1 = new ImageView(down1); ImageView Down2 = new ImageView(down2);
        ImageView Left0 = new ImageView(left0); ImageView Left1 = new ImageView(left1); ImageView Left2 = new ImageView(left2);

        Up0.setFitHeight(h); Up0.setFitWidth(w); Up1.setFitHeight(h); Up1.setFitWidth(w); Up2.setFitHeight(h); Up2.setFitWidth(w);
        Right0.setFitHeight(h); Right0.setFitWidth(w); Right1.setFitHeight(h); Right1.setFitWidth(w); Right2.setFitHeight(h); Right2.setFitWidth(w);
        Left0.setFitHeight(h); Left0.setFitWidth(w); Left1.setFitHeight(h); Left1.setFitWidth(w); Left2.setFitHeight(h); Left2.setFitWidth(w);
        Down0.setFitHeight(h); Down0.setFitWidth(w); Down1.setFitHeight(h); Down1.setFitWidth(w); Down2.setFitHeight(h); Down2.setFitWidth(w);

        Up = new Group(Up0, Up1, Up2); Right = new Group(Right0, Right1, Right2); Down = new Group(Down0, Down1, Down2); Left = new Group(Left0, Left1, Left2);

        up = new Timeline(); right = new Timeline(); down = new Timeline(); left = new Timeline();

        up.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent e) -> Up.getChildren().setAll(Up0)));
        up.getKeyFrames().add(new KeyFrame(Duration.millis(200), (ActionEvent e) -> Up.getChildren().setAll(Up1)));
        up.getKeyFrames().add(new KeyFrame(Duration.millis(300), (ActionEvent e) -> Up.getChildren().setAll(Up2)));
        right.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent e) -> Right.getChildren().setAll(Right0)));
        right.getKeyFrames().add(new KeyFrame(Duration.millis(200), (ActionEvent e) -> Right.getChildren().setAll(Right1)));
        right.getKeyFrames().add(new KeyFrame(Duration.millis(300), (ActionEvent e) -> Right.getChildren().setAll(Right2)));
        down.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent e) -> Down.getChildren().setAll(Down0)));
        down.getKeyFrames().add(new KeyFrame(Duration.millis(200), (ActionEvent e) -> Down.getChildren().setAll(Down1)));
        down.getKeyFrames().add(new KeyFrame(Duration.millis(300), (ActionEvent e) -> Down.getChildren().setAll(Down2)));
        left.getKeyFrames().add(new KeyFrame(Duration.millis(100), (ActionEvent e) -> Left.getChildren().setAll(Left0)));
        left.getKeyFrames().add(new KeyFrame(Duration.millis(200), (ActionEvent e) -> Left.getChildren().setAll(Left1)));
        left.getKeyFrames().add(new KeyFrame(Duration.millis(300), (ActionEvent e) -> Left.getChildren().setAll(Left2)));

        up.play(); right.play(); down.play(); left.play();

        up.setCycleCount(10); right.setCycleCount(10); down.setCycleCount(10); left.setCycleCount(10);

        tUp = new TranslateTransition(); tRight = new TranslateTransition(); tDown = new TranslateTransition(); tLeft = new TranslateTransition();

        tUp.setNode(Up); tRight.setNode(Right); tDown.setNode(Down); tLeft.setNode(Left);

        tUp.setDuration(Duration.millis(3000));
        tRight.setDuration(Duration.millis(3000));
        tDown.setDuration(Duration.millis(3000));
        tLeft.setDuration(Duration.millis(3000));

    }

    public void setCor(int x, int y, int m, int b) {
        x = x*m+b; y = y*m+b;
        Up.setTranslateX(x);
        Up.setTranslateY(y);
        Right.setTranslateX(x);
        Right.setTranslateY(y);
        Down.setTranslateX(x);
        Down.setTranslateY(y);
        Left.setTranslateX(x);
        Left.setTranslateY(y);
    }

    public void walkForward(int c) {
        mainTimeLine.play();
        tUp.setByY(-c);
        tRight.setByX(c);
        tDown.setByY(c);
        tLeft.setByX(-c);
        mainTransition.play();
    }

    public void setMainMode(char dir) {
        switch (dir) {
            case 'N':
                mainGroup = Up;
                mainTimeLine = up;
                mainTransition = tUp;
                break;
            case 'E':
                mainGroup = Right;
                mainTimeLine = right;
                mainTransition = tRight;
                break;
            case 'S':
                mainGroup = Down;
                mainTimeLine = down;
                mainTransition = tDown;
                break;
            case 'W':
                mainGroup = Left;
                mainTimeLine = left;
                mainTransition = tLeft;
        }
    }

    public void render(Group rg) { rg.getChildren().add(mainGroup); }
    public void remove(Group rg) { rg.getChildren().remove(mainGroup); }

}