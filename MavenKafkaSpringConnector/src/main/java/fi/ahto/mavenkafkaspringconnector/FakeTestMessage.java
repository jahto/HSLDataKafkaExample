/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.ahto.mavenkafkaspringconnector;

/**
 *
 * @author jah
 */
public class FakeTestMessage {
    private String message;

    public FakeTestMessage() {
        this.message = null;
    }
    
    public FakeTestMessage(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
    
    
}
