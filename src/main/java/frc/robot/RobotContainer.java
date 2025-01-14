// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.math.MathUtil;

import edu.wpi.first.wpilibj.XboxController;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

import frc.robot.constants.OIConstants;
import frc.robot.commands.AlignToTagPhotonVision;
import frc.robot.commands.DriveWhileAligning;
import frc.robot.subsystems.DriveSubsystem;
import frc.utils.OnTheFlyPathing;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SequentialCommandGroup;
import edu.wpi.first.wpilibj2.command.WaitCommand;
import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.JoystickButton;
import edu.wpi.first.wpilibj2.command.button.POVButton;

import com.pathplanner.lib.auto.AutoBuilder;
import com.pathplanner.lib.auto.NamedCommands;
import com.pathplanner.lib.path.PathPlannerPath;

/*
 * This class is where the bulk of the robot should be declared.  Since Command-based is a
 * "declarative" paradigm, very little robot logic should actually be handled in the {@link Robot}
 * periodic methods (other than the scheduler calls).  Instead, the structure of the robot
 * (including subsystems, commands, and button mappings) should be declared here.
 */
public class RobotContainer {
  // The robot's subsystems
  private DriveSubsystem m_robotDrive;

  private RobotShared m_robotShared = RobotShared.getInstance();

  PathPlannerPath m_ExamplePath = PathPlannerPath.fromPathFile("Example Path");

  // The driver's controller
  CommandXboxController m_driverController;

  SendableChooser<Command> autoChooser;

  /**
   * The container for the robot. Contains subsystems, OI devices, and commands.
   */
  public RobotContainer() {
    initSubsystems();
    initInputDevices();


    //Creates sendable chooser for use with PathPlanner autos
    autoChooser = AutoBuilder.buildAutoChooser();

    //Must register commands used in PathPlanner autos
    NamedCommands.registerCommand("AlignToTagPhotonVision", new AlignToTagPhotonVision());
    NamedCommands.registerCommand("GroundIntake", new InstantCommand()); //place holder
    NamedCommands.registerCommand("ShootSpeaker", new InstantCommand()); //place holder

    // Configure the button bindings
    configureButtonBindings();

    // Configure default commands
    m_robotDrive.setDefaultCommand(
      // The left stick controls translation of the robot.
      // Turning is controlled by the X axis of the right stick.
      // If any changes are made to this, please update DPad driver controls
      new RunCommand(
        () -> m_robotDrive.drive(
          -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
          -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
          -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband),
          true, true, OIConstants.kUseQuadraticInput),
        m_robotDrive));
      // NEW command NOT UPDATED DPad driver controls TESTING
    // double angleOfRightStick = Math.atan2(-MathUtil.applyDeadband(m_driverController.getRightY(), OIConstants.kDriveDeadband),
    //   -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband));
    // m_robotDrive.setDefaultCommand(
    //   new RunCommand(
    //     () -> m_robotDrive.drive(
    //       -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
    //       -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
    //       (m_robotDrive.getHeading() - angleOfRightStick),
    //       true, true, OIConstants.kUseQuadraticInput),
    //     m_robotDrive));
  }

  private void initSubsystems() {
    // New ------------------------------------------------------------------------------------------
    m_robotShared = RobotShared.getInstance();

    m_robotDrive = m_robotShared.getDriveSubsystem();
    // ----------------------------------------------------------------------------------------------
  }
  private void initInputDevices() {
    m_driverController = m_robotShared.getDriverController();
  }

  /**
   * Use this method to define your button->command mappings. Buttons can be
   * created by
   * instantiating a {@link edu.wpi.first.wpilibj.GenericHID} or one of its
   * subclasses ({@link
   * edu.wpi.first.wpilibj.Joystick} or {@link XboxController}), and then calling
   * passing it to a
   * {@link JoystickButton}.
   */
  private void configureButtonBindings() {
    m_driverController.x()
      .whileTrue(new RunCommand(() -> m_robotDrive.setXFormation()));

    m_driverController.a()
      .onTrue(new InstantCommand(() -> m_robotDrive.zeroHeading()));

    //Robot relative mode
    m_driverController.leftBumper()
      .whileTrue(new RunCommand(
        () -> m_robotDrive.drive(
          -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband),
          -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband),
          -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband),
          false, true, OIConstants.kUseQuadraticInput),
        m_robotDrive));
    
    //Half Speed mode
    m_driverController.rightBumper()
      .whileTrue(new RunCommand(
        () -> m_robotDrive.drive(
          -MathUtil.applyDeadband(m_driverController.getLeftY(), OIConstants.kDriveDeadband) / 2,
          -MathUtil.applyDeadband(m_driverController.getLeftX(), OIConstants.kDriveDeadband) / 2,
          -MathUtil.applyDeadband(m_driverController.getRightX(), OIConstants.kDriveDeadband) / 2,
          true, true, OIConstants.kUseQuadraticInput),
        m_robotDrive));
    

    //Testing path following
    m_driverController.b()
      .whileTrue(new SequentialCommandGroup(
        new InstantCommand(() -> m_robotDrive.resetOdometry(m_ExamplePath.getPreviewStartingHolonomicPose())),
        AutoBuilder.followPath(m_ExamplePath)
      ));
      
    m_driverController.y()
      .whileTrue(
        OnTheFlyPathing.getOnTheFlyPath(0, 0)
      );

    m_driverController.rightStick()
      .onTrue(new SequentialCommandGroup(
        // double normalizedAngle = (int)((m_robotDrive.getHeading() + 180) / (360 / 8)),  // This is the uncondensed code
        new DriveWhileAligning((int)((m_robotDrive.getHeading() + 180) / (360 / 8)), true, true).withTimeout(3))
      );
    // Something super janky is happening here but it works so
    for(int angleForDPad = 0; angleForDPad <= 7; angleForDPad++) { // Sets all the DPad to rotate to an angle
      new POVButton(m_driverController.getHID(), angleForDPad * 45)
        .onTrue(new SequentialCommandGroup(
          new DriveWhileAligning(angleForDPad * -45, true, true).withTimeout(3), // -45 could be 45 
          new WaitCommand(.1))); // small delay to prevent reinput after angled DPad input
    }
  }

  /**
   * Use this to pass the autonomous command to the main {@link Robot} class.
   *
   * @return the command to run in autonomous
   */
  public Command getAutonomousCommand() {
    return autoChooser.getSelected();
  }
}
