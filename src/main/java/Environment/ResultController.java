package Environment;

public class ResultController {
}


//    // make a bottom pane which display trials information
//    Text agentRowLabel = new Text("AGENT");
//		agentRowLabel.setFont(Font.font("Freestyle Script", 30));
//
//    Text wumpusRowLabel = new Text("WUMPUS");
//		wumpusRowLabel.setFont(Font.font("Mistral",30));
//
//    Circle spaceHolder = new Circle(60);
//		spaceHolder.setFill(Color.CYAN);
//
//    Circle w_spaceHolder = new Circle(60);
//		w_spaceHolder.setFill(Color.CRIMSON);
//
//    HBox bottomBox = new HBox(new VBox(new StackPane(spaceHolder, agentRowLabel), new StackPane(w_spaceHolder, wumpusRowLabel)));
//
//    int totalScore = 0;
//    int w_totalScore = 0;
//
//		for (int i = 0; i < numTrials; i++) {
//
//        totalScore += trialScores[i];
//        w_totalScore += trialWumpusScores[i];
//
//        Text agentResult = new Text("Trial " + (i + 1) + " score: " + trialScores[i]);
//        agentResult.setFont(Font.font("Trebuchet MS", 20));
//
//        Text wumpusResult = new Text("Trial " + (i + 1) + " score: " + trialWumpusScores[i]);
//        wumpusResult.setFont(Font.font("Trebuchet MS", 20));
//
//        Rectangle agentBG = new Rectangle(220, 120);
//        agentBG.setFill(Color.CYAN);
//        agentBG.setArcHeight(50);
//        agentBG.setArcWidth(50);
//
//        Rectangle wumpusBG = new Rectangle(220, 120);
//        wumpusBG.setFill(Color.CRIMSON);
//        wumpusBG.setArcHeight(50);
//        wumpusBG.setArcWidth(50);
//
//        bottomBox.getChildren().add(new VBox(new StackPane(agentBG, agentResult), new StackPane(wumpusBG, wumpusResult)));
//
//    }
//
//
//    // this goes on top pane
//    Text finalMessage = new Text(
//            "Agent << Total Score: " + totalScore +
//                    "\t\tAverage Score: " + ((double) totalScore / (double) numTrials) +
//                    "\n\nWumpus << Total Score: " + w_totalScore +
//                    "\t\tAverage Score: " + ((double) w_totalScore / (double) numTrials));
//		finalMessage.setFont(Font.font("Trebuchet MS", 20));

