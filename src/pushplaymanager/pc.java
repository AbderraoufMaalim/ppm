/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pushplaymanager;

import com.jfoenix.controls.JFXButton;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;

public class pc {
    private int idPc;
    private int numPc;
    private Pane pane;

    public pc(int idPc, int numPc, Pane btn) {
        this.idPc = idPc;
        this.numPc = numPc;
        this.pane = btn;
    }
    
    public pc(int idPc, int numPc) {
        this.idPc = idPc;
        this.numPc = numPc;
        JFXButton editbtn = new JFXButton("Pause");
        editbtn.setPrefWidth(50);
        Label label = new Label(getNumPc()+"");
        
        this.pane = new Pane(editbtn,label);
    }

    public int getIdPc() {
        return idPc;
    }

    public void setIdPc(int idPc) {
        this.idPc = idPc;
    }

    public int getNumPc() {
        return numPc;
    }

    public void setNumPc(int numPc) {
        this.numPc = numPc;
    }

    public Pane getBtn() {
        return pane;
    }

    public void setBtn(Pane btn) {
        this.pane = btn;
    }
  
    
}
