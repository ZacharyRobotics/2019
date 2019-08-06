/*----------------------------------------------------------------------------*/
/* Copyright (c) 2017-2018 FIRST. All Rights Reserved.                        */
/* Open Source Software - may be modified and shared by FRC teams. The code   */
/* must be accompanied by the FIRST BSD license file in the root directory of */
/* the project.                                                               */
/*----------------------------------------------------------------------------*/

package frc.robot;

import edu.wpi.first.wpilibj.ADXRS450_Gyro;
import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.cameraserver.CameraServer; // NOTE: I changed this from same package
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.TimedRobot; // NOTE: Changed from iterative
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.Spark;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

// TODO: Test timed vs. iterative
public class Robot extends TimedRobot {
	public SendableChooser<String> driveSelector = new SendableChooser<>();
	public SendableChooser<String> gyroSelector = new SendableChooser<>();
	public SendableChooser<String> pistonSelector = new SendableChooser<>();
	public DoubleSolenoid horizontal = new DoubleSolenoid(0,1);
	public Compressor comp = new Compressor();
	public ADXRS450_Gyro gyro = new ADXRS450_Gyro(SPI.Port.kOnboardCS0);
	
	public Joystick joystick = new Joystick(0);
	public int miniJoystick;
	public Boolean firstRun = true;

	public long lastToggleTime;
	public Boolean driveToggle = false;
	public Boolean previousDriveToggle = false;
	public Boolean currentDriveToggle = false;
	
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
		/*UsbCamera front = CameraServer.getInstance().startAutomaticCapture(0);
		front.setResolution(128, 240);
		front.setFPS(30);

		UsbCamera back = CameraServer.getInstance().startAutomaticCapture(0);
		back.setResolution(128, 240);
		back.setFPS(30);*/
		// Webcam

		CameraServer.getInstance().startAutomaticCapture(0);
		CameraServer.getInstance().startAutomaticCapture(1);
		
		driveSelector.setDefaultOption("Point", "Point"); // addDefault
		driveSelector.addOption("Point w/ speed control", "Point w/ speed control"); // addObject
		driveSelector.addOption("Z-Axis w/ speed control", "Z-Axis w/ speed control");
		driveSelector.addOption("Z-Axis", "Z-Axis");
		SmartDashboard.putData("Driving styles", driveSelector);
		
		gyroSelector.setDefaultOption("0 : 360", "0 : 360");
		gyroSelector.addOption("-180 : 180", "-180 : 180");
		SmartDashboard.putData("Gyro Selector", gyroSelector);
		
		pistonSelector.setDefaultOption("Manual", "Manual");
		pistonSelector.addOption("Automatic", "Automatic");
		SmartDashboard.putData("Piston Selector", pistonSelector);

		comp.start();
		
		leftSide = new Spark(0);
		rightSide = new Spark(1);
		
	}
	
	@Override
	public void autonomousPeriodic() {
		teleopPeriodic();
	}
	
	@Override
	public void teleopPeriodic() {
		steering();
		gyroDisplay();
		managePayload();
	}
	
	public void steering() {
		String driveStyle = driveSelector.getSelected();
		previousDriveToggle = currentToggleButton;
		currentDriveToggle = joystick.getRawButton(2); // Joystick side button
		
		// Only toggles if it has been more than half a second after the last toggle
		if (currentDriveToggle && !previousDriveToggle && System.currentTimeMillis() >= lastToggleTime + 500) {
			driveToggle = driveToggle ? false : true;
			lastToggleTime = System.currentTimeMillis();
		}
		
		// Is there a way to toggle the currently selected method? (On Drive Station)
		if (driveToggle) {
			if (driveStyle == "Point") {
				driveStyle = "Z-Axis";
			} else if (driveStyle == "Z-Axis") {
				driveStyle = "Point";
			}
		}
		
		
		previousToggleButton = currentToggleButton;
		currentToggleButton = joystick.getRawButton(1); // Trigger

		if (currentToggleButton && !previousToggleButton) {
			toggle = toggle ? false : true;
		}
		
		if (toggle) {
			SmartDashboard.putString("SPEED TOGGLED: ", "YES");
		} else {
			SmartDashboard.putString("SPEED TOGGLED: ", "NO");
		}
		
		// Leave it as two spaces b/c unique identifier
		SmartDashboard.putString("  ", "  " + driveStyle);
		
		if (driveStyle == "Point") {
			// This makes the joystick work where the robot just goes where you point the stick
			// PLAYERS: Jude
			
			double left = joystick.getX() + joystick.getY();
			double right = joystick.getX() - joystick.getY();
			leftSide.set(toggle ? left * .5 : left);
			rightSide.set(toggle ? right * .5 : right);
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
			
			
			double left = joystick.getZ() + joystick.getY();
			double right = joystick.getZ() - joystick.getY();
			leftSide.set(toggle ? left * .5 : left);
			rightSide.set(toggle ? right * .5 : right);
		}
	}
	
	public void gyroDisplay() {
		 //Handles the gyro display for the Smart Dashboard
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
		String pistonStyle = pistonSelector.getSelected();
		
		miniJoystick = joystick.getPOV();
		if (miniJoystick == 315 || miniJoystick == 0 || miniJoystick == 45) {
			SmartDashboard.putString("Piston: ", " OUT");
			horizontal.set(DoubleSolenoid.Value.kReverse);
			
			if (pistonStyle == "Automatic") {
				// Pushes piston out and pulls it in
				try {
					Thread.sleep(1500);
					horizontal.set(DoubleSolenoid.Value.kForward);
					SmartDashboard.putString("Piston: ", " IN");
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		} else if (miniJoystick == 135 || miniJoystick == 180 || miniJoystick == 225) {
			SmartDashboard.putString("Piston: ", " IN");
			horizontal.set(DoubleSolenoid.Value.kForward);
		} else {
			if (firstRun) {
				SmartDashboard.putString("Piston: ", " IN");
				firstRun = false;
			}
			
			// If piston automatically triggers, move air compressor start to here...
			horizontal.set(DoubleSolenoid.Value.kOff);
		}
	}
}
