/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package org.usfirst.frc.team6489.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.first.wpilibj.CameraServer;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.IterativeRobot;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Robot extends IterativeRobot {
	private SendableChooser<String> driveSelector = new SendableChooser<>();
	private SendableChooser<String> gyroSelector = new SendableChooser<>();
	public DoubleSolenoid horizontal = new DoubleSolenoid(3,2);
	public Compressor comp = new Compressor();
	private ADXRS450_Gyro gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	
	Joystick joystick = new Joystick(0);
	public int miniJoystick;

	public Boolean toggle = false;
	public Boolean previousToggleButton = false;
	public Boolean currentToggleButton = false;

	/** Negative is forward **/
	Spark leftSide;
	/** Positive is forward **/
	Spark rightSide;

	@Override
	public void robotInit() {
		gyro.calibrate();
		gyro.reset(); // Only rezeroes a second time if you restart the robot
		
		// Red line to show center of camera
		SmartDashboard.putString(" ", " ");
		CameraServer.getInstance().startAutomaticCapture(0);
		CameraServer.getInstance().startAutomaticCapture(1);
		
		driveSelector.addDefault("Point", "Point");
		driveSelector.addObject("Point w/ speed control", "Point w/ speed control");
		driveSelector.addObject("Z-Axis w/ speed control", "Z-Axis w/ speed control");
		driveSelector.addObject("Z-Axis", "Z-Axis");
		SmartDashboard.putData("Driving styles", driveSelector);
		
		gyroSelector.addDefault("0 : 360", "0 : 360");
		gyroSelector.addObject("-180 : 180", "-180 : 180");
		SmartDashboard.putData("Gyro Selector", gyroSelector);

		comp.start();
		
		leftSide = new Spark(0);
		rightSide = new Spark(1);
	}
	
	@Override
	public void teleopPeriodic() {
		steering();
		gyroDisplay();
		managePayload();
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
			// PLAYERS:
			
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
						System.out.println("xPos; yPos");
						leftSide.set(speed);
						rightSide.set(-speed + xValue);
					} else {
						System.out.println("xNeg; yPos");
						leftSide.set(speed + xValue);
						rightSide.set(-speed); // -speed + xValue
					}
				} else if (yValue < -.35) { // Backwards
					if (xPos) {
						System.out.println("xPos; yPos");
						leftSide.set(speed / 2);
						rightSide.set((-speed - xValue) / 2);
					} else {
						System.out.println("xNeg; yPos");
						leftSide.set((speed - xValue) / 2);
						rightSide.set(-speed / 2);
					}
				} else { // Stationary
					if (xPos) { // Right side of joystick
						System.out.println("xPos; yNeg");
						leftSide.set(speed);
						rightSide.set(speed);
					} else { // Left side of joystick
						System.out.println("xNeg; yNeg");
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
			// PLAYERS: Parker, Dylan
			
			previousToggleButton = currentToggleButton;
			currentToggleButton = joystick.getRawButton(1); // Trigger

			if (currentToggleButton && !previousToggleButton) {
				toggle = toggle ? false : true;
			}
			
			double left = joystick.getZ() + joystick.getY();
			double right = joystick.getZ() - joystick.getY();
			leftSide.set(toggle ? left * .5 : left);
			rightSide.set(toggle ? right * .5 : right);
		}
	}
	
	public void gyroDisplay() {
		// Handles the gyro display for the Smart Dashboard
		String gyroStyle = gyroSelector.getSelected();
		double angle = Math.floor(gyro.getAngle());
		if (gyroStyle == "0 : 360") {
			if (gyro.getAngle() > 360 || gyro.getAngle() < -360) {
				gyro.reset();
				SmartDashboard.putNumber("Gyro angle ", angle);
			} else if (gyro.getAngle() < 0) {
				SmartDashboard.putNumber("Gyro angle ", 360 + angle);
			} else {
				SmartDashboard.putNumber("Gyro angle ", angle);
			}
		} else if (gyroStyle == "-180 : 180") {
			if (gyro.getAngle() > 360 || gyro.getAngle() < -360) {
				gyro.reset();
				SmartDashboard.putNumber("Gyro angle ", angle);
			} else if (angle > -180 && angle <= 180) {
				SmartDashboard.putNumber("Gyro angle ", angle);
			} else if (angle < -180) {
				SmartDashboard.putNumber("Gyro angle ", 360 + angle);
			} else if (angle > 180) {
				SmartDashboard.putNumber("Gyro angle ", angle - 360);
			}
		}
	}
	
	public void managePayload() {
		miniJoystick = joystick.getPOV();
		if (miniJoystick == 0 || miniJoystick == 45 || miniJoystick == 315) {
			// Pushes piston out and pulls it in after half a second
			horizontal.set(DoubleSolenoid.Value.kForward);
			try {
				Thread.sleep(500);
				horizontal.set(DoubleSolenoid.Value.kReverse);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			horizontal.set(DoubleSolenoid.Value.kOff);
		}
	}
}
