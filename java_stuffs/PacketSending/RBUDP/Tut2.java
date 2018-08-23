/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author 19843151
 */
public class Tut2 extends Thread{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new FileServer(7000).start();		
        new senderFrame("localhost", 7000, "m.mp3").setVisible(true);
       
    }
    
}
