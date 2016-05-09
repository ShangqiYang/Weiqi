import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
public class MainWindow extends Frame implements Runnable{
    Go panelGo=new Go();
    Image myImage;
    int PORT;
    Socket sendSocket;//Connect to Socket
    PrintWriter writer;//send message
    boolean stopFlag;
    boolean isInitiative;
    Point messagePoint;
    Point goStartPoint=null;
    Point yellowPoint=null;
    boolean stepColor=true;
    Point LastPoint=null;//judge the change of point when remove yellow point
    BorderLayout borderLayout1 = new BorderLayout();
    Panel panel1 = new Panel();
    Panel panel2 = new Panel();
    BorderLayout borderLayout2 = new BorderLayout();
    Panel panel3 = new Panel();
    CheckboxGroup checkboxGroup1 = new CheckboxGroup();
    Checkbox checkbox1 = new Checkbox();
    Checkbox checkbox2 = new Checkbox();
    Label label1 = new Label();
    TextField textField1 = new TextField();
    Button button1 = new Button();
    Label label2 = new Label();
    Choice choice1 = new Choice();
    Button button2 = new Button();
    GridLayout gridLayout1 = new GridLayout();
    BorderLayout borderLayout3 = new BorderLayout();
    public MainWindow(){
        try{
            jbInit();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    private void jbInit() throws Exception{
        choice1.setBackground(new Color(255, 255, 255));
        button1.setBackground(new Color(0, 255, 0));
 //       try
 //       {
 //          UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 //       }catch(Exception e){e.printStackTrace();}
        this.setResizable(false);
        new Thread(this).start();//Start thread
        this.PORT=1976;
        this.isInitiative=false;//Connect or not
        this.stopFlag=false;//Continue to listen or not
        this.choice1.setFont(new java.awt.Font("Dialog",1,15));
        this.choice1.addItem("BLACK STONE");
        this.choice1.setFont(new java.awt.Font("Dialog",1,15));
        this.choice1.addItem("WHITE STONE");
        LastPoint=new Point();
        messagePoint=new Point();
        this.setSize(810,950);
        this.setTitle("Chinese Go    EE810 JAVA");
        this.panelGo.setEnabled(false);//Prohibit use panel before start
        checkbox1.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                checkbox1_mouseClicked(e);
            }
        });
        this.goStartPoint=this.panelGo.getLocation();//
        this.setLayout(borderLayout1);
        panel1.setLayout(borderLayout2);
        checkbox1.setFont(new java.awt.Font("Dialog",1,25));
        checkbox1.setCheckboxGroup(checkboxGroup1);
        checkbox1.setLabel("Offline Game");
        checkbox2.setFont(new java.awt.Font("Dialog",1,25));
        checkbox2.setCheckboxGroup(checkboxGroup1);
        checkbox2.setLabel("Online Game");
        checkbox2.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                checkbox2_mouseClicked(e);
            }
        });
        label1.setFont(new java.awt.Font("Dialog",1,15));
        label1.setText("The Opponent Address");
        button1.setFont(new java.awt.Font("Dialog",1,25));
        button1.setLabel("Connect");
        button1.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(ActionEvent e){
                button1_actionPerformed(e);
            }
        });
        label2.setText("  ");
        button2.setFont(new java.awt.Font("Dialog",1,25));
        button2.setBackground(new Color(255, 0, 255));
        button2.setLabel("Start");
        button2.addActionListener(new java.awt.event.ActionListener(){
            public void actionPerformed(ActionEvent e){
                button2_actionPerformed(e);
            }
        });
        panel3.setLayout(gridLayout1);
        gridLayout1.setRows(2);
        gridLayout1.setColumns(4);
        gridLayout1.setHgap(10);
        gridLayout1.setVgap(20);
        panel2.setLayout(borderLayout3);
        this.panel2.setSize(500,70);
        panelGo.addMouseMotionListener(new java.awt.event.MouseMotionAdapter(){
            public void mouseMoved(MouseEvent e){
                panelGo_mouseMoved(e);
            }
        });
        panelGo.addMouseListener(new java.awt.event.MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                panelGo_mouseClicked(e);
            }
        });
        this.addWindowListener(new java.awt.event.WindowAdapter(){
            public void windowClosing(WindowEvent e){
                this_windowClosing(e);
            }
        });
        panel3.setBackground(new Color(236, 190, 98));
        panel3.add(checkbox1, null);
        panel3.add(checkbox2, null);
        panel3.add(label1, null);
        panel3.add(textField1, null);
        panel3.add(button2, null);
        panel3.add(label2, null);
        panel3.add(button1, null);
        panel3.add(choice1, null);
        this.panel1.add(this.panelGo,BorderLayout.CENTER);
        this.panel1.add(panel3, BorderLayout.NORTH);
        this.add(panel2, BorderLayout.SOUTH);
        this.add(panel1, BorderLayout.CENTER);
        this.disableLink();
        this.checkboxGroup1.setSelectedCheckbox(this.checkbox1);
        this.yellowPoint=new Point(1000,1000);//Initialize a yellow point
	    this.centerWindow();
        this.show();
        myImage=this.createImage(32,32);//Store previous yellow point
    }
    void centerWindow(){
	  Dimension d=Toolkit.getDefaultToolkit().getScreenSize();
	  int pX=(d.width-this.getWidth())/2;
	  int pY=(d.height-this.getHeight())/2;
	  this.setLocation(pX,pY);
    }
    public static void main(String args[]){
        MainWindow main=new MainWindow();
    }
    //Listen thread
    public void run(){
        try{
            ServerSocket serverSocket=new ServerSocket(PORT);
            Socket receiveSocket=null;
            receiveSocket=serverSocket.accept();
            if(this.isInitiative)//If it is connected, cannot cennct to an other
                this.stopFlag=true;
            this.checkboxGroup1.setSelectedCheckbox(this.checkbox2);//auto choose online game
            this.button1.setEnabled(false);
            this.choice1.setEnabled(true);
            this.textField1.setEnabled(false);
            this.checkbox1.setEnabled(false);
            this.checkbox2.setEnabled(false);
            this.writer=new PrintWriter(receiveSocket.getOutputStream(),true);
            BufferedReader reader=new BufferedReader(new InputStreamReader(receiveSocket.getInputStream()));
            String message;
            while(!this.stopFlag){
                this.panelGo.showError("Connection is successful£¡");
                message=reader.readLine();
                this.doMessage(message);
            }
            reader.close();
            receiveSocket.close();
            serverSocket.close();
        }catch(IOException ioe){this.panelGo.showError("Unexcepted Interruption");}
    }
    //Processing received information
    void doMessage(String message){
        if(message.startsWith("start")){
            this.panelGo.showError("The other has begun");
            if(message.equals("start_black"))
                this.choice1.select("WHITE STONE");
            else
                this.choice1.select("BLACK STONE");
            if(this.choice1.getSelectedItem().equals("BLACK STONE"))//Black stone start first
                this.panelGo.setEnabled(true);
            this.paintMyColor();//Display the color
            this.disableLink();
        }
        else{//Stone information
            int color=Integer.parseInt(message.substring(0,1));
            this.messagePoint.x=Integer.parseInt(message.substring(1,3));
            this.messagePoint.y=Integer.parseInt(message.substring(3,5));
            this.panelGo.setEnabled(true);
            this.panelGo.doStep(this.messagePoint,color);
        }
    }
    //Positioning mouse 
    void panelGo_mouseMoved(MouseEvent e){
        Point realPoint=e.getPoint();
        Point mousePoint=this.panelGo.getMousePoint(realPoint,this.goStartPoint);
        this.removeLastMousePoint(this.LastPoint,mousePoint);
        this.LastPoint.x=mousePoint.x;
        this.LastPoint.y=mousePoint.y;
        if(this.isPlace(mousePoint))
            this.showMousePoint(mousePoint);
    }
    //Add yellow point range
    boolean isPlace(Point p){
        if(p.x>19||p.x<1||p.y<1||p.y>19)
            return false;
        int color;
        One one;
        one=(One)(this.panelGo.myHash.get(p));
        color=one.color;
        if(color!=0)
            return false;
        return true;
    }
    void panelGo_mouseClicked(MouseEvent e){
        if(this.isSingle()){
            this.doSingle();
        }
        else{
            this.doMultiple();
        }
    }
    //Start
    void button2_actionPerformed(ActionEvent e){
        if(e.getActionCommand().equals("Start")){
            this.disableLink();
            this.checkbox1.setEnabled(false);
            this.checkbox2.setEnabled(false);
            this.button2.setLabel("Exit");
            if(this.isSingle())
                this.panelGo.setEnabled(true);
            else{
                if(this.choice1.getSelectedItem().equals("BLACK STONE")){
                    this.writer.println("start_black");
                }
                else
                    this.writer.println("start_white");
            }
            this.paintMyColor();//Display color
        }
        else if(e.getActionCommand().equals("Exit")){
            this.dispose();
            System.exit(0);
        }
    }
    //disable online control link
    void disableLink(){
        this.textField1.setBackground(new Color(236, 190, 98));
        this.textField1.setEnabled(false);
        this.choice1.setEnabled(false);
        this.button1.setEnabled(false);
    }
    //enable online control link
    void enableLink(){
        this.textField1.setBackground(Color.white);
        this.textField1.setFont(new java.awt.Font("Dialog",1,25));
        this.textField1.setEnabled(true);
        this.choice1.setEnabled(true);
        this.button1.setEnabled(true);
    }
    //Judge which type
    boolean isSingle(){
        return this.checkbox1.getState();
    }
    void single(){
    }
    void multiple(){
    }
    //Add yellow point
    void showMousePoint(Point mousePoint){
        Graphics g=this.panelGo.getGraphics();
        g.setColor(Color.yellow);
        g.fillOval(mousePoint.x*40-12,mousePoint.y*40-12,this.panelGo.INTERVAL-16,this.panelGo.INTERVAL-16);
        this.yellowPoint.x=mousePoint.x;//located yellow point
        this.yellowPoint.y=mousePoint.y;
        Graphics myG=this.myImage.getGraphics();
        this.createMyImage(myG,this.yellowPoint,0);
    }
    //remove last yellow point
    void removeLastMousePoint(Point thatPoint,Point thisPoint){
        if(thatPoint.x!=thisPoint.x||thatPoint.y!=thisPoint.y){
            Graphics g=this.panelGo.getGraphics();
            if(this.yellowPoint!=null&&this.myImage!=null)
                g.drawImage(this.myImage,this.yellowPoint.x*40-16,this.yellowPoint.y*40-16,32,32,null);
            this.yellowPoint.x=1000;//if not in the range, remove
            this.yellowPoint.y=1000;
        }	
    }
    //Create Images
    void createMyImage(Graphics g,Point thisPoint,int color){
        int px=thisPoint.x;
        int py=thisPoint.y;
        Color myColor=this.panelGo.getBackground();
        if(px==1&&py==1&&color==0){//Four corners
        	g.setColor(myColor);
            g.fillRect(0,0,32,32);
            g.setColor(Color.black);
            g.drawLine(16,16,16,32);
            g.drawLine(10,10,10,32);
            g.drawLine(16,16,32,16);
            g.drawLine(10,10,32,10);
        }
        else if(px==1&&py==19&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,8*2,0);
            g.drawLine(8*2,8*2,16*2,8*2);
            g.drawLine(5*2,11*2,16*2,11*2);
            g.drawLine(5*2,11*2,5*2,0);
        }
        else if(px==19&&py==1&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,8*2,16*2);
            g.drawLine(8*2,8*2,0,8*2);
            g.drawLine(11*2,5*2,11*2,16*2);
            g.drawLine(11*2,5*2,0,5*2);
        }
        else if(px==19&&py==19&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,8*2,0);
            g.drawLine(8*2,8*2,0,8*2);
            g.drawLine(11*2,11*2,11*2,0);
            g.drawLine(11*2,11*2,0,11*2);
        }
        else if(px==1&&color==0){//four edges
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,16*2,8*2);
            g.drawLine(8*2,0,8*2,16*2);
            g.drawLine(5*2,0,5*2,16*2);
        }
        else if(px==19&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,0,8*2);
            g.drawLine(8*2,0,8*2,16*2);
            g.drawLine(11*2,0,11*2,16*2);
        }
        else if(py==1&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,8*2,16*2);
            g.drawLine(0,8*2,16*2,8*2);
            g.drawLine(0,5*2,16*2,5*2);
        }
        else if(py==19&&color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(8*2,8*2,8*2,0);
            g.drawLine(0,8*2,16*2,8*2);
            g.drawLine(0,11*2,16*2,11*2);
        }
        //Nine litter black point
        else if(color==0&&((px==4&&py==4)||(px==4&&py==10)||(px==4&&py==16)||(px==10&&py==4)||(px==10&&py==10)||(px==10&&py==16)||(px==16&&py==4)||(px==16&&py==10)||(px==16&&py==16))){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(0,8*2,16*2,8*2);
            g.drawLine(8*2,0,8*2,16*2);
            g.fillOval(5*2,5*2,6*2,6*2);
        }
        else if(color==0){
            g.setColor(myColor);
            g.fillRect(0,0,16*2,16*2);
            g.setColor(Color.black);
            g.drawLine(0,8*2,16*2,8*2);
            g.drawLine(8*2,0,8*2,16*2);
        }
    }
    //Single Model
    void doSingle(){
        if(this.stepColor)
            this.panelGo.doStep(this.yellowPoint,1);
        else
            this.panelGo.doStep(this.yellowPoint,2);
        if(!this.panelGo.errorFlag){//if can place, change color
            this.stepColor=!this.stepColor;
            this.paintThisColor(this.stepColor);
        }
        else
            this.panelGo.errorFlag=false;
        this.yellowPoint.x=1000;
        this.yellowPoint.y=1000;

    }
    //Multiple model
    void doMultiple(){
        int color;
        if(this.choice1.getSelectedItem().equals("BLACK STONE"))
            color=1;
        else
            color=2;
        this.panelGo.doStep(this.yellowPoint,color);
        //if cannot place stone, return
        if(this.panelGo.errorFlag){
            this.panelGo.errorFlag=false;
            return;
        }
        this.panelGo.setEnabled(false);
        String message=this.getMessage(color,this.yellowPoint.x,this.yellowPoint.y);
        this.writer.println(message);
        this.yellowPoint.x=99;
        this.yellowPoint.y=99;
    }
    //Processing sent string
    String getMessage(int color,int x,int y){
        String strColor=String.valueOf(color);
        String strX;
        String strY;
        if(x<10)
            strX="0"+String.valueOf(x);
        else
            strX=String.valueOf(x);

        if(y<10)
            strY="0"+String.valueOf(y);
        else
            strY=String.valueOf(y);

        return strColor+strX+strY;
    }
    void this_windowClosing(WindowEvent e){
        this.dispose();
        System.exit(0);
    }
    void checkbox2_mouseClicked(MouseEvent e){
        this.enableLink();
    }
    void checkbox1_mouseClicked(MouseEvent e){
        this.disableLink();
    }
    void button1_actionPerformed(ActionEvent e){
        this.goToLink(this.textField1.getText().trim(),this.PORT);
    }
    //connect to serverSocket
    void goToLink(String hostName,int port){
        try{
            this.stopFlag=true;
            this.sendSocket=new Socket(hostName,port);
            this.panelGo.showError("Connection is successful£¡");
            this.choice1.setEnabled(true);
            this.button1.setEnabled(false);
            this.checkbox1.setEnabled(false);
            this.checkbox2.setEnabled(false);
            this.textField1.setEnabled(false);
            this.writer=new PrintWriter(this.sendSocket.getOutputStream(),true);
            new Listen(sendSocket,this).start();
        }catch(IOException ioe){this.panelGo.showError("Unexcepted Interruption");}
    }
    void paintMyColor(){
        Graphics g=this.label2.getGraphics();
        if(this.choice1.getSelectedItem().equals("BLACK STONE"))
            g.fillOval(0,0,35,35);
        else
        {
            g.setColor(Color.white);
            g.fillOval(0,0,35,35);
            g.setColor(Color.black);
            g.drawOval(0,0,35,35);
        }
    }
    void paintThisColor(boolean whatColor){
        Graphics g=this.label2.getGraphics();
        if(whatColor)
            g.fillOval(0,0,35,35);
        else{
            g.setColor(Color.white);
            g.fillOval(0,0,35,35);
            g.setColor(Color.black);
            g.drawOval(0,0,35,35);
        }
    }
}

