import java.awt.*;
import java.util.*;
//import javax.swing.*;
//import java.awt.image.*;
public class Go extends Panel{
    int whichStep;
    Hashtable myHash;
    Point pointNow;//Current point
    Point STARTPOINT;
    int INTERVAL;
    Vector vec;
    Point robPoint;//ko
    Point mousePoint;
    boolean errorFlag;
    public Go(){
        super();
        pointNow=new Point(1000,1000);//Stippling the initial red point outside
        errorFlag=false;
        whichStep=0;
        STARTPOINT=new Point(40,40);
        INTERVAL=40;
        myHash=new Hashtable();
        robPoint=null;//ko point
        mousePoint=new Point();
        vec=new Vector();//Store stone that need to be checked
        this.initMyHash(STARTPOINT,INTERVAL);
        try{
            jbInit();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    //initialize Hashtable
    void initMyHash(Point startPoint,int interval){
        One one;
        Point key;//Logical point mark
        int i,j;
        for(i=1;i<=19;i++)
            for(j=1;j<=19;j++){
                key=new Point(i,j);
                one=new One();
                one.posX=startPoint.x+(i-1)*interval;
                one.posY=startPoint.y+(j-1)*interval;
                //Get adjacent points
                one.pointAround[0]=new Point(i,j-1);//Up
                one.pointAround[1]=new Point(i,j+1);//Down
                one.pointAround[2]=new Point(i-1,j);//Left
                one.pointAround[3]=new Point(i+1,j);//Right
                if(i==1)one.pointAround[2]=one.OUT;
                if(i==19)one.pointAround[3]=one.OUT;
                if(j==1)one.pointAround[0]=one.OUT;
                if(j==19)one.pointAround[1]=one.OUT;
                myHash.put(key,one);
            }
    }
    //Update Panel
    public void paint(Graphics g){
        Point startPoint=STARTPOINT;
        int interval=INTERVAL;
        this.paintChessboard(g,startPoint,interval);
        this.paintChessman(g,startPoint,interval);
    }
    //Draw Chessboard
    void paintChessboard(Graphics g,Point startPoint,int interval){
        int pX=startPoint.x;
        int pY=startPoint.y;
        int LINELENGTH=interval*18;
        int i;
        for(i=0;i<19;i++){
            g.drawLine(pX+i*interval,pY,pX+i*interval,pY+LINELENGTH);
            g.drawLine(pX,pY+i*interval,pX+LINELENGTH,pY+i*interval);
        }
        g.fillOval(pX+interval*3-6,pY+interval*3-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*9-6,pY+interval*3-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*15-6,pY+interval*3-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*3-6,pY+interval*9-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*9-6,pY+interval*9-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*15-6,pY+interval*9-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*3-6,pY+interval*15-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*9-6,pY+interval*15-6,(int)(interval-28),(int)(interval-28));
        g.fillOval(pX+interval*15-6,pY+interval*15-6,(int)(interval-28),(int)(interval-28));
        g.drawRect(pX-6,pY-6,732,732);
    }
    //Add Stone
    void paintChessman(Graphics g,Point startPoint,int interval){
        int pX=startPoint.x;
        int pY=startPoint.y;
        Enumeration enun=myHash.elements();
        while(enun.hasMoreElements()){
            One one=(One)enun.nextElement();
            if(one.color!=one.BLANK){
                if(one.color==one.BLACK)
                    g.setColor(Color.black);
                else if(one.color==one.WHITE)
                    g.setColor(Color.white);
                else
                    break;
                g.fillOval(one.posX-16,one.posY-16,interval-8,interval-8);
                g.setColor(Color.black);
                g.drawOval(one.posX-16,one.posY-16,interval-8,interval-8);
            }
        }
        g.setColor(Color.red);//Draw red point
        g.fillOval(this.pointNow.x*40-5,this.pointNow.y*40-5,10,10);
    }
    //Processing every step
    void doStep(Point whatPoint,int whatColor){
        //If the point is out of the chessboard, return
        if(whatPoint.x<1||whatPoint.x>19||whatPoint.y<1||whatPoint.y>19){
            this.showError("Can't be place here!");
            this.errorFlag=true;
            return;
        }
         //If the point already had stone, return
        if(((One)myHash.get(whatPoint)).color!=0){
            this.showError("There's already a Stone!");
            this.errorFlag=true;
            return;
        }
        if(this.isRob(whatPoint)){
            this.showError("There's a created ko, Please end this ko first!");
            this.errorFlag=true;
            return;
        }
        this.updateHash(whatPoint,whatColor);
        this.getRival(whatPoint,whatColor);
        //If there is no liberty and same type
        if(!this.isLink(whatPoint,whatColor)&&!this.isLink(whatPoint,0)){//0 is equal to One.BKANK{
            this.showError("Can't be place here!");
            this.errorFlag=true;
            this.singleRemove(whatPoint);
            return;
        }
        this.pointNow.x=whatPoint.x;
        this.pointNow.y=whatPoint.y;//Get current red point
        this.repaint();
    }
    //remove different type stone and judge to capture stone or not
    void getRival(Point whatPoint,int whatColor){
        boolean removeFlag=false;//Determine to capture stone or not in this step
        One one;
        one=(One)(this.myHash.get(whatPoint));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++){
            One otherOne=(One)(this.myHash.get(otherPoint[i]));//Get different type stone
            if(!otherPoint[i].equals(one.OUT))
                if(otherOne.color!=one.BLANK&&otherOne.color!=whatColor){
                    if(this.isRemove(otherPoint[i]))//If there is liberty
                        this.vec.clear();
                    else{
                            this.makeRobber(otherPoint[i]);
                            this.doRemove();
                            this.vec.clear();
                            removeFlag=true;
                    }
                }
        }
        if(!removeFlag)
            this.robPoint=null;//If not capture stone, cancel ko point 

    }
    //Determine the reason that cannot place stone is ko or not 
    boolean isRob(Point p){
        if(this.robPoint==null)
            return false;
        if(this.robPoint.x==p.x&&this.robPoint.y==p.y)
            return true;
        return false;
    }
    //Create ko point 
    void makeRobber(Point point){
        if(this.vec.size()==1)
                this.robPoint=point;//Create new ko point
        else
            this.robPoint=null;//cannel ko point
    }
    //Determine to capture stone
    boolean isRemove(Point point){
        if(this.vec.contains(point))
            return false;
        if(this.isLink(point,0))//There is liberty
            return true;
        this.vec.add(point);//If there is no liberty, add this point
        One one;
        one=(One)(this.myHash.get(point));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++){
            One otherOne=(One)(this.myHash.get(otherPoint[i]));//Get same type stone
            if(!otherPoint[i].equals(one.OUT))
                if(otherOne.color==one.color)
						if(this.isRemove(otherPoint[i]))//recursion
                        return true;
        }
        return false;
    }
    //remove stone from board
    void doRemove(){
        Enumeration enun=this.vec.elements();
        while(enun.hasMoreElements()){
            Point point=(Point)enun.nextElement();
            this.singleRemove(point);
        }
    }
    //remove single stone from board
    void singleRemove(Point point){
        One one=(One)(this.myHash.get(point));
        one.isthere=false;
        one.color=one.BLANK;
        Graphics g=this.getGraphics();
        g.clearRect(point.x*20-8,point.y*20-8,20,20);//delete stone
    }
    //Judge there is liberty or not
    boolean isLink(Point point,int color){
        One one;
        one=(One)(this.myHash.get(point));
        Point otherPoint[]=one.pointAround;
        int i;
        for(i=0;i<4;i++){
            One otherOne=(One)(this.myHash.get(otherPoint[i]));
            if(!otherPoint[i].equals(one.OUT))
                if(otherOne.color==color){
                    return true;
                }
        }
        return false;
    }
    //Update Hashtable every step
    void updateHash(Point whatPoint,int whatColor){
        One one=(One)(this.myHash.get(whatPoint));
        one.isthere=true;
        one.color=whatColor;
        this.whichStep=this.whichStep+1;
        one.whichStep=this.whichStep;
    }
    //Calculate the logical point using rounding
    //p1 is the actual point, p2 is correspinding point
    Point getMousePoint(Point p1,Point p2){
        this.mousePoint.x=Math.round((float)(p1.x-p2.x)/this.INTERVAL);
        this.mousePoint.y=Math.round((float)(p1.y-p2.y)/this.INTERVAL);
        return this.mousePoint;
    }
    //Show error information
    void showError(String errorMessage){
        Graphics g=this.getGraphics();
        g.setColor(new Color(151,255,255));
        g.fillRect(40,800,200,25);
        g.setColor(Color.red);
        g.drawString(errorMessage,80,815);
        g.fillOval(40,800,20,20);
    }
    private void jbInit() throws Exception{
        this.setBackground(new Color(236, 255, 98));
    }
}
