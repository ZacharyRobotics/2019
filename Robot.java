/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team6489.robot;

import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
//import edu.wpi.first.wpilibj.I2C;

public class Robot extends IterativeRobot {
	private SendableChooser<String> driveSelector = new SendableChooser<>();
	//public DoubleSolenoid vertical = new DoubleSolenoid(3,2);
	public DoubleSolenoid horizontal = new DoubleSolenoid(3,2);
	public Compressor comp = new Compressor();
	
	Joystick joystick = new Joystick(0);
	public static boolean openVert;
	public static boolean closeVert;
	public int miniJoystick;
	
	//I2C colorSensor;
	
	/** Negative is forward **/
	Spark leftSide;
	/** Positive is forward **/
	Spark rightSide;

	@Override
	public void robotInit() {
		CameraServer.getInstance().startAutomaticCapture();
		
		driveSelector.addDefault("Point", "Point");
		driveSelector.addObject("Point w/ speed control", "Point w/ speed control");
		driveSelector.addObject("Z-Axis w/ speed control", "Z-Axis w/ speed control");
		driveSelector.addObject("Z-Axis", "Z-Axis");
		SmartDashboard.putData("Driving styles", driveSelector);
		
		//colorSensor = new I2C(I2C.Port.kOnboard, 0x3C);
		
		comp.start();
		
		leftSide = new Spark(0);
		rightSide = new Spark(1);
	}
	
	@Override
	public void teleopPeriodic() {
		steering();
		
		// Horizontal
		miniJoystick = joystick.getPOV();
		if (miniJoystick == 0 || miniJoystick == 45 || miniJoystick == 315) {
			horizontal.set(DoubleSolenoid.Value.kForward);
		} else if (miniJoystick == 135 || miniJoystick == 180 || miniJoystick == 225) {
			horizontal.set(DoubleSolenoid.Value.kReverse);
		} else {
			horizontal.set(DoubleSolenoid.Value.kOff);
		}
		
		// Vertical
		/*openVert = joystick.getRawButton(1);
		closeVert = joystick.getRawButton(2);
		
		if (openVert) {
			vertical.set(DoubleSolenoid.Value.kForward);
		} else if (closeVert) {
			vertical.set(DoubleSolenoid.Value.kReverse);
		} else {
			vertical.set(DoubleSolenoid.Value.kOff);
		}*/
	}
	
	public void steering() {
		String driveStyle = driveSelector.getSelected();
		
		if (driveStyle == "Point") {
			// This makes the joystick work where the robot just goes where you point the stick
			// PLAYERS: Jude
			
			leftSide.set(joystick.getX() + joystick.getY());
			rightSide.set(joystick.getX() - joystick.getY());
		} else if (driveStyle == "Point w/ speed control") {
			// Slider determines speed with point to drive
			// PLAYERS: Parker, Payton
			
			double speed = (-joystick.getThrottle() + 1) / 2; // Negative because the controls are reversed
			if (speed < .2) {
				speed = .2;
			}
			
			double xValue = joystick.getX();
			double yValue = -joystick.getY();
			Boolean inXDeadzone = xValue < .15 && xValue > -.15;
			if (inXDeadzone) {
				if (yValue > .2) { // Forwards
					leftSide.set(-speed);
					rightSide.set(speed);
				} else if (yValue < -.2) { // Backwards
					leftSide.set(speed);
					rightSide.set(-speed);
				} else { // Stationary
					leftSide.set(0);
					rightSide.set(0);
				}
			} else {
				Boolean xPos = xValue > 0;
				if (yValue > .35) { // Forwards
					if (xPos) {
						leftSide.set(-speed);
						rightSide.set(speed - xValue); // Multiply?
					}
				} else if (yValue < -.35) { // Backwards
					leftSide.set(speed);
					rightSide.set(-speed);
				} else { // Stationary
					if (xPos) { // Right side of joystick
						leftSide.set(speed);
						rightSide.set(speed);
					} else { // Left side of joystick
						leftSide.set(-speed);
						rightSide.set(-speed);
					}
				}
			}
		} else if (driveStyle == "Z-Axis w/ speed control") {
			// Slider determines speed with z-axis turning
			// PLAYERS: 
			
			double speed = (joystick.getThrottle() / 2) - .7;
			if (joystick.getY() < -.3 || joystick.getZ() > .2 || joystick.getZ() < -.2) { // Forwards
				if (joystick.getZ() > .2 || joystick.getZ() < -.2) {
					leftSide.set((joystick.getZ() + speed) * .5);
					rightSide.set((joystick.getZ() - speed) * .5);
				} else {
					leftSide.set(speed);
					rightSide.set(-speed);
				}
			} else if (joystick.getY() > .3) { // Backwards
				// This math sets the speed to zero when the throttle is -1.
				if (joystick.getZ() > .2 || joystick.getZ() < -.2) {
					leftSide.set((joystick.getZ() - speed) * .5);
					rightSide.set((joystick.getZ() + speed) * .5);
				} else {
					leftSide.set(-speed);
					rightSide.set(speed);
				}
			} else {
				leftSide.set(0);
				rightSide.set(0);
			}
		} else if (driveStyle == "Z-Axis") {
			// Makes forward/backward use Y axis and turning use Z axis
			// PLAYERS: 
			
			leftSide.set((joystick.getZ() + joystick.getY()) * .5);
			rightSide.set((joystick.getZ() - joystick.getY()) * .5);
		}
	}
}
