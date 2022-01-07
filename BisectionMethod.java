import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

class DrawerPanel extends JPanel
{  
	static int SX = 50;
	static int SY = 50;
	static int WIDTH = 500;
	static int HEIGHT = 500;
	static int OX = SX + WIDTH/10;
	static int OY = SY + HEIGHT;
	static int XWIDTH = WIDTH + WIDTH/10;
	static int YHEIGHT = HEIGHT + HEIGHT/10;
	static int MARKINGSIZE = 5;
	static int MAXDOMAIN = 10;
	static int MAXRANGE = 10;

	static Random rand = new Random();

	Point2D.Double low;
	Point2D.Double high;
	Point2D.Double mid = new Point2D.Double();
	Point2D.Double from = new Point2D.Double();
	Point2D.Double p1 = new Point2D.Double();
	Point2D.Double p2 = new Point2D.Double();

	double distance = 0.0;
	double minDistance = 10e10;
	double minX = 10e10;
	double minY = 10e10;
	double ERROR_BOUND = 0.001;
	boolean lastDraw = false;

	DrawerPanel() {
		p1.x = rand.nextInt(MAXDOMAIN);
		p1.y = rand.nextInt(MAXRANGE);

		p2.x = rand.nextInt(MAXDOMAIN);
		p2.y = rand.nextInt(MAXRANGE);
		
		from.x = rand.nextInt(MAXDOMAIN);
		from.y = rand.nextInt(MAXRANGE);
/*
p1.x = 2.0; p1.y = 2.0;
p2.x = 2.0; p2.y = 6.0;
from.x = 4.0;
from.y = 4.0;
*/
		minDistance = getDistance(from,p1);
		double otherDistance = getDistance(from,p2);

		if (minDistance < otherDistance)
		{
			low = new Point2D.Double(p1.x,p1.y); high = new Point2D.Double(p2.x,p2.y);
		} else {
			low = new Point2D.Double(p2.x,p2.y); high = new Point2D.Double(p1.x,p1.y);
			minDistance = otherDistance;
		}

		Thread thread = new Thread() {
			public void run() {
				while(true) {
					mid.x = (low.x + high.x) / 2.0;
					mid.y = (low.y + high.y) / 2.0;

					double midDistance = getDistance(from,mid);
					if (getDistance(mid,low) < ERROR_BOUND || 
						getDistance(mid,high) < ERROR_BOUND)
					{
						lastDraw = true;
						minX = mid.x;
						minY = mid.y;
						break;
					}

					double stepX = 0.9*mid.x + 0.1*high.x;
					double stepY = 0.9*mid.y + 0.1*high.y;

					Point2D.Double stepUp = new Point2D.Double(stepX,stepY);
					double upDistance = getDistance(from,stepUp);

					stepX = 0.9*mid.x + 0.1*low.x;
					stepY = 0.9*mid.y + 0.1*low.y;

					Point2D.Double stepDown = new Point2D.Double(stepX,stepY);
					double downDistance = getDistance(from,stepDown);

					if (upDistance < midDistance)
					{
						low.x = mid.x;
						low.y = mid.y;
						minDistance = midDistance;
					} else if (downDistance < midDistance)
					{
						high.x = mid.x;
						high.y = mid.y;
						minDistance = midDistance;
					} else {
						lastDraw = true;
						minX = mid.x;
						minY = mid.y;
						break;
					}

					try
					{
						Thread.sleep(1000);
					}
					catch (InterruptedException ex)
					{
					}					
					repaint();
				}
				lastDraw = true;
				repaint();
			}
		};
		thread.start();
	}
	double getDistance(Point2D.Double p,Point2D.Double q) {
		return Math.sqrt((p.x-q.x)*(p.x-q.x) + (p.y-q.y)*(p.y-q.y));
	}
	void drawAxes(Graphics g) {
		// draw X axis
		g.drawLine(SX,SY+HEIGHT,SX+XWIDTH,SY+HEIGHT);
		// draw Y axis
		g.drawLine(OX,SY,OX,SY+YHEIGHT);

		// draw interval
		int i;
		int interval = WIDTH/10;
		g.drawLine(OX-interval,OY,OX-interval,OY-MARKINGSIZE);
		g.drawLine(OX,OY+interval,OX+MARKINGSIZE,OY+interval);
		for(i = 1; i <= 10; i++) {
			g.drawLine(OX+i*interval,OY,
				OX+i*interval,OY-MARKINGSIZE);
			g.drawLine(OX,OY-i*interval,
				OX+MARKINGSIZE,OY-i*interval);
		}

		String maxDomain = "" + MAXDOMAIN;
		g.drawString(maxDomain,SX+XWIDTH,SY+HEIGHT-10);
		String maxRange = "" + MAXRANGE;
		g.drawString(maxRange,OX+10,SY);
	}
	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);

		drawAxes(g);

		double maxDomain = (double)MAXDOMAIN;
		double maxRange = (double)MAXRANGE;

		int xPrime1 = OX + (int)(p1.x*WIDTH/maxDomain);
		int yPrime1 = (int)(-(p1.y*HEIGHT/(maxRange)) + OY);
		g.drawOval(xPrime1-2,yPrime1-2,4,4);
		String pos = "" + "(" + p1.x + "," + p1.y + ")";
		g.drawString(pos,xPrime1+2,yPrime1+13);

		int xPrime2 = OX + (int)(p2.x*WIDTH/maxDomain);
		int yPrime2 = (int)(-(p2.y*HEIGHT/(maxRange)) + OY);
		g.drawOval(xPrime2-3,yPrime2-3,6,6);
		pos = "" + "(" + p2.x + "," + p2.y + ")";
		g.drawString(pos,xPrime2+2,yPrime2+13);

		int xPrimeFrom = OX + (int)(from.x*WIDTH/maxDomain);
		int yPrimeFrom = (int)(-(from.y*HEIGHT/(maxRange)) + OY);
		g.setColor(Color.red);
		g.drawOval(xPrimeFrom-2,yPrimeFrom-2,4,4);
		pos = "" + "(" + from.x + "," + from.y + ")";
		g.drawString(pos,xPrimeFrom+2,yPrimeFrom+13);

		g.setColor(Color.blue);
		g.drawLine(xPrime1,yPrime1,xPrime2,yPrime2);

		g.setColor(Color.green);

		double x = mid.x;
		double y = mid.y;
		int xPrime = OX + (int)(x*WIDTH/maxDomain);
		int yPrime = (int)(-(y*HEIGHT/(maxRange)) + OY);

		g.drawLine(xPrimeFrom,yPrimeFrom,xPrime,yPrime);

		if (lastDraw)
		{
			g.setColor(Color.red);
			xPrime = OX + (int)(minX*WIDTH/maxDomain);
			yPrime = (int)(-(minY*HEIGHT/(maxRange)) + OY);
			g.drawLine(xPrimeFrom,yPrimeFrom,xPrime,yPrime);
			g.setColor(Color.black);
		}
	}
}
class GraphDrawerFrame extends JFrame
{
	GraphDrawerFrame() 
	{
		setTitle("Distance from a point to a line segment");
		setSize(700,700);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container contentPane = this.getContentPane();
		contentPane.add(new DrawerPanel());
	}
}
public class BisectionMethod
{  
    public static void main(String[] args)
	{
		JFrame frame = new GraphDrawerFrame();
		frame.setVisible(true);  
    }
}

