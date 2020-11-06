package localization.view;

import localization.MapEventListener;
import localization.models.MapEvent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SettingsPanel extends JPanel implements MapEventListener {

    private JCheckBox possibleCheckBox;
    private JCheckBox bestMoveCheckBox;
    private JLabel bestMoveLabel;
    private JLabel positionText;
    private LocalizationMapView view;
    public SettingsPanel(LocalizationMapView view) {
        super(true);
        this.view = view;
        this.setLayout(new GridLayout(2, 0));

        initPossibleCheckbox();
        initBestMoveCheckBox();
        initPlayerLocation();
        initBestMoveLabel();
    }

    private void initBestMoveLabel() {
        this.bestMoveLabel = new JLabel();
        this.add(bestMoveLabel);
    }

    private void initBestMoveCheckBox() {
        this.bestMoveCheckBox = new JCheckBox("Suggest Best Move? (Alpha...)");
        this.bestMoveCheckBox.setSelected(true);
        this.bestMoveCheckBox.addActionListener((a) ->
        {
            if(bestMoveCheckBox.isSelected())
                setBestMovement("(Waiting for new movement)");
            else
                setBestMovement("(Disabled)");
        });
        this.add(this.bestMoveCheckBox);
    }

    public void setBestMovement(String movement)
    {
        bestMoveLabel.setText("Next Move: " + movement);
    }

    public boolean shouldAutoMove()
    {
        return this.bestMoveCheckBox.isSelected();
    }

    private void initPlayerLocation() {
        this.positionText = new JLabel("Position: (X, Y)");

        this.add(positionText);
    }

    private void initPossibleCheckbox() {
        this.possibleCheckBox = new JCheckBox("Show Possible Locations?");
        this.possibleCheckBox.addActionListener(this::checkBoxSelected);
        this.possibleCheckBox.setSelected(true);
        this.add(this.possibleCheckBox);
    }

    private void checkBoxSelected(ActionEvent actionEvent) {
        if(!actionEvent.getSource().equals(this.possibleCheckBox))
            return;

        view.update();

    }


    public boolean showPossible()
    {
        return this.possibleCheckBox.isSelected();
    }

    @Override
    public void agentMoved(MapEvent event) {
        var agentLoc = event.getNewLocation();
        this.positionText.setText("Position: (" + agentLoc.x + ", " + agentLoc.y +")");
    }
}
