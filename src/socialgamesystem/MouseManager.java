package socialgamesystem;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class MouseManager implements MouseListener, MouseMotionListener{

	private boolean leftPressed, rightPressed, clicked;
	private int mouseX, mouseY;
	
	public MouseManager() {
	}
	
	public void update() {
		//this.clicked = false;
	}
	
	public void mouseClicked(MouseEvent e) {
		//this.clicked = true;
	}

	public void mouseEntered(MouseEvent e) {
		
		
	}

	public void mouseExited(MouseEvent e) {
		
	}

	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			this.clicked = true;
			leftPressed = true;
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			rightPressed = true;
		}
		
	}

	public void mouseReleased(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			this.clicked = false;
			leftPressed = false;
		}
		else if (e.getButton() == MouseEvent.BUTTON3) {
			rightPressed = false;
		}
	}

	@Override
	public void mouseDragged(MouseEvent e) {
		
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		mouseX = e.getX();
		mouseY = e.getY();
	}
	
	public boolean isClicked() {
		return clicked;
	}

	public boolean isLeftPressed() {
		return leftPressed;
	}

	public boolean isRightPressed() {
		return rightPressed;
	}

	public int getMouseX() {
		return mouseX;
	}

	public int getMouseY() {
		return mouseY;
	}

}
