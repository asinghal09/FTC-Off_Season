package org.firstinspires.ftc.teamcode;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.hardware.bosch.JustLoggingAccelerationIntegrator;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DcMotorController;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
//import org.firstinspires.ftc.teamcode.Vectors;
import java.util.*;

@Autonomous
public class MecanumAuto extends LinearOpMode {

    DcMotor rightFront = null, rightBack = null, leftFront = null, leftBack = null;

    BNO055IMU imu;
    Orientation angles;
    double globAng;
    int rfPos, rbPos, lfPos, lbPos;
    double rfPow, rbPow, lfPow, lbPow;
    double rfRev, rbRev, lfRev, lbRev;
    double rfScalPow, rbScalPow, lfScalPow, lbScalPow;
    double initTime, deltaTime;
    double Kp = 0.02, Ki = 0.01, i = 0, Kd = 0.001, d = 0, pre_error = 0, PIDpow;
    Vector currentPoint;

    private final int TICKS = 1120;

    @Override
    public void runOpMode() throws InterruptedException {
        //assign each motor to the port in config
        rightFront = hardwareMap.dcMotor.get("rf");
        rightBack = hardwareMap.dcMotor.get("rb");
        leftFront = hardwareMap.dcMotor.get("lf");
        leftBack = hardwareMap.dcMotor.get("lb");

        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        rightFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        rightBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftFront.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        leftBack.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);


        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.angleUnit = BNO055IMU.AngleUnit.DEGREES;
        parameters.accelUnit = BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC;
        parameters.calibrationDataFile = "BNO055IMUCalibration.json"; // see the calibration sample opmode
        parameters.loggingEnabled = true;
        parameters.loggingTag = "IMU";
        parameters.accelerationIntegrationAlgorithm = new JustLoggingAccelerationIntegrator();

        // initializes the IMU
        imu = hardwareMap.get(BNO055IMU.class, "imu");
        imu.initialize(parameters);
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);

        waitForStart();
        /*
        drive(0.3, 0, 1, -1, 0, 0.0);
        brake(400);
        drive(0.3, 0, -1, 1, 0, 0.0);
        brake(400);

        for(int i = 90; i >= -180; i = i-90){
            driveVector(i, 1, 0.3);
            brake(400);
        }*/

        for(int i =0; i<5;i++){
            drive(0.5,-1,1,-1,1,0.0);
            brake(500);
            drive(0.5,1,-1,1,-1,0.0);
            brake(500);
        }

    }

    //this method can be used to test the encoders in the future in case encoders go out of phase
    public void testEncoder(int rev){
        int tics = TICKS*(rev);
        telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition()));
        telemetry.update();

        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rightFront.setMode(DcMotor.RunMode.RESET_ENCODERS);

        rightFront.setTargetPosition(-tics);
        rfPos = rightFront.getCurrentPosition();

        //this is a bad idea, it has abuilt in PID which messes up mecanum wheels
        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        while(Math.abs(rfPos) < Math.abs(tics)){
            rightFront.setPower(0.1);
            rfPos = rightFront.getCurrentPosition();
            telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition()));
            telemetry.update();
        }
    }

    public void joyRide(double rev, double power){
        int tics = (int)(TICKS*rev);

        telemetry.addData("1", "rfMotor encoder:" + String.format("%d", rightFront.getCurrentPosition()));
        telemetry.addData("2", "rbMotor encoder:" + String.format("%d", rightBack.getCurrentPosition()));
        telemetry.addData("3", "lfMotor encoder:" + String.format("%d", leftFront.getCurrentPosition()));
        telemetry.addData("4", "lbMotor encoder:" + String.format("%d", leftBack.getCurrentPosition()));
        telemetry.update();

        rightFront.setMode(DcMotor.RunMode.RESET_ENCODERS);
        rightBack.setMode(DcMotor.RunMode.RESET_ENCODERS);
        leftFront.setMode(DcMotor.RunMode.RESET_ENCODERS);
        leftBack.setMode(DcMotor.RunMode.RESET_ENCODERS);

        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);

        rfPos = rightFront.getCurrentPosition();
        rbPos = rightBack.getCurrentPosition();
        lfPos = leftFront.getCurrentPosition();
        lbPos = leftBack.getCurrentPosition();

        while(Math.abs(rfPos) < Math.abs(tics)){
            powDrive(power,power,-power,-power);

            rfPos = rightFront.getCurrentPosition();
            rbPos = rightBack.getCurrentPosition();
            lfPos = leftFront.getCurrentPosition();
            lbPos = leftBack.getCurrentPosition();
            telemetry.addData("1", "rfMotor encoder:" + String.format("%d", rightFront.getCurrentPosition()));
            telemetry.addData("2", "rbMotor encoder:" + String.format("%d", rightBack.getCurrentPosition()));
            telemetry.addData("3", "lfMotor encoder:" + String.format("%d", leftFront.getCurrentPosition()));
            telemetry.addData("4", "lbMotor encoder:" + String.format("%d", leftBack.getCurrentPosition()));
            telemetry.update();
        }
    }

    /**
     * freedrive is just for testing new drive variations
     *
     * @param pow: takes in the power that each motor will be set to
     * @param rfRev: number of revolutions for the right front motor
     * @param rbRev: number of revolutions for the right back motor
     * @param lfRev: number of revolutions for the left front motor
     * @param lbRev: number of revolutions for the left back motor
     */
    public void testDrive(double pow, double rfRev, double rbRev, double lfRev,double lbRev){
        //need to change to a distace parameter
        //so for the parameters are the number of revolutions for each motor, but with future testing and aditional methods
        //which will make this freeDrive method a helper one, we can program based on distance rather than revolutions
        //currently distance is not fesibel due to inconsistancies(wheel slipage) and drifting(hardware issue)
        int rfTics = (int)(rfRev*TICKS);
        int rbTics = (int)(rbRev*TICKS);
        int lfTics = (int)(lfRev*TICKS);
        int lbTics = (int)(lbRev*TICKS);

        rfPow = (rfRev*(pow))/(double)Math.abs(rfRev);
        rbPow = (rbRev*(pow))/(double)Math.abs(rbRev);
        lfPow = (lfRev*(pow))/(double)Math.abs(lfRev);
        lbPow = (lbRev*(pow))/(double)Math.abs(lbRev);

        double rfDir, rbDir, lfDir, lbDir;

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double initAng = Math.abs(angles.firstAngle);
        //int tic = (dist/(wheel_diameter))*TICKS;
        //NOTE: rev = dist/(wheel_diameter)

        telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                + " target: " + String.format("%d", rfTics));
        telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                + " target: "+String.format("%d", rbTics));
        telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                + " target: "+String.format("%d", lfTics));
        telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                + " target: "+String.format("%d", lbTics));
        telemetry.addData("5", "initial angle: " + Double.toString(initAng));
        telemetry.update();

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        initAng = Math.abs(angles.firstAngle);

        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rfPos = rightFront.getCurrentPosition();
        rbPos = rightBack.getCurrentPosition();
        lfPos = leftFront.getCurrentPosition();
        lbPos = leftBack.getCurrentPosition();

        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        //while loop must use || because when using && if a revolution of 0 is given to one motor, the statement
        //short circuits and stops, but the || allows to run each motor at individual revolutions
        while((Math.abs(rfPos) < Math.abs(rfTics)) || (Math.abs(rbPos) < Math.abs(rbTics))||
                (Math.abs(lfPos) < Math.abs(lfTics))|| (Math.abs(lbPos) < Math.abs(lbTics))){

            //angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            //double curAng = angles.firstAngle;

            //double error = curAng - initAng;
            //double correction = (Kp*error);
            //double encoder_Error_Correction = same as the scale Pow calculation
            double correction = 0;
            if(Math.abs(rfPos) != Math.abs(rfTics)){
                rfDir = ((Math.abs(rfTics)-Math.abs(rfPos))/(Math.abs((Math.abs(rfTics)-Math.abs(rfPos)))));
                rfScalPow = rfDir * (Range.clip(rfPow*((double)Math.abs(rfTics)-(double)(Math.abs(rfPos))/(double)Math.abs(rfTics)),-pow,pow))-(correction);
            }else{ rfScalPow = 0.0; }

            if(Math.abs(rbPos) != Math.abs(rbTics)){
                rbDir = ((Math.abs(rbTics)-Math.abs(rbPos))/(Math.abs((Math.abs(rbTics)-Math.abs(rbPos)))));
                rbScalPow = rbDir * (Range.clip(rbPow*((double)Math.abs(rbTics)-(double)(Math.abs(rbPos))/(double)Math.abs(rbTics)),-pow,pow))-(correction);
            }else{ rbScalPow = 0.0; }

            if(Math.abs(lfPos) != Math.abs(lfTics)){
                lfDir = ((Math.abs(lfTics)-Math.abs(lfPos))/(Math.abs((Math.abs(lfTics)-Math.abs(lfPos)))));
                lfScalPow = lfDir * (Range.clip(lfPow*((double)Math.abs(lfTics)-(double)(Math.abs(lfPos))/(double)Math.abs(lfTics)),-pow,pow))-(correction);
            }else{ lfScalPow = 0.0; }

            if(Math.abs(lbPos) != Math.abs(lbTics)){
                lbDir = ((Math.abs(lbTics)-Math.abs(lbPos))/(Math.abs((Math.abs(lbTics)-Math.abs(lbPos)))));
                lbScalPow = lbDir * (Range.clip(lbPow*((double)Math.abs(lbTics)-(double)(Math.abs(lbPos))/(double)Math.abs(lbTics)),-pow,pow))-(correction);
            }else{ lbScalPow = 0.0; }

            powDrive(rfScalPow,rbScalPow,lfScalPow,lbScalPow);

            rfPos = rightFront.getCurrentPosition();
            rbPos = rightBack.getCurrentPosition();
            lfPos = leftFront.getCurrentPosition();
            lbPos = leftBack.getCurrentPosition();

            //deltaTime = getRuntime() - initTime;
            //if (Math.abs(error) < 30){ i += Ki * error * deltaTime;}
            //if (i > 0.3) { i = 0.3;}

            telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                    + " target: " + String.format("%d", rfTics)
                    + " power: " + Double.toString(Math.round(rfScalPow*100)/100.0));
            telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                    + " target: "+String.format("%d", rbTics)
                    + " power: " + Double.toString(Math.round(rbScalPow*100)/100.0));
            telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                    + " target: "+String.format("%d", lfTics)
                    + " power: " + Double.toString(Math.round(lfScalPow*100)/100.0));
            telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                    + " target: "+String.format("%d", lbTics)
                    + " power: " + Double.toString(Math.round(lbScalPow*100)/100.0));
            telemetry.addData("5", "initial angle: " + Double.toString(initAng));
            telemetry.addData("6", "current angle: " + Double.toString(curAng));
            telemetry.addData("7", "error: " + Double.toString(error));
            telemetry.addData("8", "correction: " + Double.toString(correction));
            telemetry.update();
        }
        //need to reset the mode of the motors so that it can work with other methods such as powDrive
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //brake(50);
        //angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        //double endAng = Math.abs(angles.firstAngle);
        //turn(endAng-initAng, 0, 0.3);
        brake(100);
    }

    /**
     * Mukilan K. 2020.08.11
     * drive allows control over direction and number of rotations of each indivitual motor
     * also fixed limitations form previous drive method which required each motor to spin the same non zero amount of times
     * would always spin the least number of revolution int he parameter (previous method will be replaced once freeDrive is tested)
     *
     * @param pow: takes in the power that each motor will be set to
     * @param rfRev: number of revolutions for the right front motor
     * @param rbRev: number of revolutions for the right back motor
     * @param lfRev: number of revolutions for the left front motor
     * @param lbRev: number of revolutions for the left back motor
     */
    public void drive(double pow, double rfRev, double rbRev, double lfRev,double lbRev, double target){
        //need to change to a distace parameter
        //so for the parameters are the number of revolutions for each motor, but with future testing and aditional methods
        //which will make this freeDrive method a helper one, we can program based on distance rather than revolutions
        //currently distance is not fesibel due to inconsistancies(wheel slipage) and drifting(hardware issue)
        int rfTics = (int)(rfRev*TICKS);
        int rbTics = (int)(rbRev*TICKS);
        int lfTics = (int)(lfRev*TICKS);
        int lbTics = (int)(lbRev*TICKS);

        rfPow = (rfRev*(pow))/(double)Math.abs(rfRev);
        rbPow = (rbRev*(pow))/(double)Math.abs(rbRev);
        lfPow = (lfRev*(pow))/(double)Math.abs(lfRev);
        lbPow = (lbRev*(pow))/(double)Math.abs(lbRev);

        double rfDir, rbDir, lfDir, lbDir;

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double initAng = Math.abs(angles.firstAngle);
        //int tic = (dist/(wheel_diameter))*TICKS;
        //NOTE: rev = dist/(wheel_diameter)

        telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                + " target: " + String.format("%d", rfTics));
        telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                + " target: "+String.format("%d", rbTics));
        telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                + " target: "+String.format("%d", lfTics));
        telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                + " target: "+String.format("%d", lbTics));
        telemetry.addData("5", "initial angle: " + Double.toString(initAng));
        telemetry.update();

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        initAng = Math.abs(angles.firstAngle);

        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rfPos = rightFront.getCurrentPosition();
        rbPos = rightBack.getCurrentPosition();
        lfPos = leftFront.getCurrentPosition();
        lbPos = leftBack.getCurrentPosition();

        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODERS);
        //while loop must use || because when using && if a revolution of 0 is given to one motor, the statement
        //short circuits and stops, but the || allows to run each motor at individual revolutions
        while((Math.abs(rfPos) < Math.abs(rfTics)) || (Math.abs(rbPos) < Math.abs(rbTics))||
                (Math.abs(lfPos) < Math.abs(lfTics))|| (Math.abs(lbPos) < Math.abs(lbTics))){

            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            double curAng = angles.firstAngle;

            initTime = getRuntime();
            double error = curAng - initAng;
            double correction = (Kp*error);
            //double encoder_Error_Correction = same as the scale Pow calculation

            if(Math.abs(rfPos) != Math.abs(rfTics)){
                rfDir = ((Math.abs(rfTics)-Math.abs(rfPos))/(Math.abs((Math.abs(rfTics)-Math.abs(rfPos)))));
                rfScalPow = (rfDir * Range.clip(rfPow*((double)Math.abs(rfTics)-(double)(Math.abs(rfPos))/(double)Math.abs(rfTics)),-pow,pow))-correction;
            }else{ rfScalPow = 0.0; }

            if(Math.abs(rbPos) != Math.abs(rbTics)){
                rbDir = ((Math.abs(rbTics)-Math.abs(rbPos))/(Math.abs((Math.abs(rbTics)-Math.abs(rbPos)))));
                rbScalPow = (rbDir * Range.clip(rbPow*((double)Math.abs(rbTics)-(double)(Math.abs(rbPos))/(double)Math.abs(rbTics)),-pow,pow))-correction;
            }else{ rbScalPow = 0.0; }

            if(Math.abs(lfPos) != Math.abs(lfTics)){
                lfDir = ((Math.abs(lfTics)-Math.abs(lfPos))/(Math.abs((Math.abs(lfTics)-Math.abs(lfPos)))));
                lfScalPow = (lfDir * Range.clip(lfPow*((double)Math.abs(lfTics)-(double)(Math.abs(lfPos))/(double)Math.abs(lfTics)),-pow,pow))-correction;
            }else{ lfScalPow = 0.0; }

            if(Math.abs(lbPos) != Math.abs(lbTics)){
                lbDir = ((Math.abs(lbTics)-Math.abs(lbPos))/(Math.abs((Math.abs(lbTics)-Math.abs(lbPos)))));
                lbScalPow = (lbDir * Range.clip(lbPow*((double)Math.abs(lbTics)-(double)(Math.abs(lbPos))/(double)Math.abs(lbTics)),-pow,pow))-correction;
            }else{ lbScalPow = 0.0; }

            powDrive(rfScalPow,rbScalPow,lfScalPow,lbScalPow);

            rfPos = rightFront.getCurrentPosition();
            rbPos = rightBack.getCurrentPosition();
            lfPos = leftFront.getCurrentPosition();
            lbPos = leftBack.getCurrentPosition();

            deltaTime = getRuntime() - initTime;
            if (Math.abs(error) < 30){ i += Ki * error * deltaTime;}
            if (i > 0.3) { i = 0.3;}

            telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                    + " target: " + String.format("%d", rfTics)
                    + " power: " + Double.toString(Math.round(rfScalPow*100)/100.0));
            telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                    + " target: "+String.format("%d", rbTics)
                    + " power: " + Double.toString(Math.round(rbScalPow*100)/100.0));
            telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                    + " target: "+String.format("%d", lfTics)
                    + " power: " + Double.toString(Math.round(lfScalPow*100)/100.0));
            telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                    + " target: "+String.format("%d", lbTics)
                    + " power: " + Double.toString(Math.round(lbScalPow*100)/100.0));
            telemetry.addData("5", "initial angle: " + Double.toString(initAng));
            telemetry.addData("6", "current angle: " + Double.toString(curAng));
            telemetry.addData("7", "error: " + Double.toString(error));
            telemetry.addData("8", "correction: " + Double.toString(correction));
            telemetry.update();
        }
        //need to reset the mode of the motors so that it can work with other methods such as powDrive
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //brake(50);
        //angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        //double endAng = Math.abs(angles.firstAngle);
        //turn(endAng-initAng, 0, 0.3);
        brake(100);
    }

    //Turn method to be written after studying the IMU, first work on robot motion
    /**
     *
     * @param target: if positive: clockwise if negative: counterclockwise
     *             this method is not motor encoder based, must write it based on an IMU PID loop
     * @param edge: parameter for determing the pivot point of the robot
     *            0: center of mass
     *            1-4; edgdes starging form front going clockwise
     *            5-8: wheels startings form right front going clockwise
     * @param pow: if positive -> anitclockwise, if pow is regative -> clockwise
     */
    public void turn(double target, int edge, double pow){
        double  offset = 90;
        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double initialPos = angles.firstAngle;

        double currAng = angles.firstAngle;

        double error = offset - Math.abs(target);
        pow = (target*pow)/(Math.abs(target));

        while (opModeIsActive() && ((Math.abs(target) - Math.abs(currAng)) > 3)) {
            initTime = getRuntime();
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            currAng = Math.abs(angles.firstAngle);

            error = currAng - Math.abs(target);
            double correction = (Kp*Math.abs(error)) + i + d;

            pow = Range.clip(pow*(Math.abs((currAng - Math.abs(target)) / (100.0)) + i), -0.7, .7);

            telemetry.addData("Current Position: ", currAng);
            telemetry.addData("Distance to go: ", error);
            telemetry.update();

            PIDpow = pow + correction;
            switch (edge) {
                case 0:
                    powDrive(pow, pow, pow, pow);
                    break;
                case 1:
                    powDrive(0.0, pow, 0.0, pow);
                    break;
                case 2:
                    powDrive(0.0, 0.0, pow, pow);
                    break;
                case 3:
                    powDrive(pow, 0.0, pow, 0.0);
                    break;
                case 4:
                    powDrive(pow, pow, 0.0, 0.0);
                    break;
                case 5:
                    powDrive(0.0, pow, pow, pow);
                    break;
                case 6:
                    powDrive(pow,0.0,pow,pow);
                    break;
                case 7:
                    powDrive(pow, pow,0.0, pow);
                    break;
                case 8:
                    powDrive(pow, pow, pow, 0.0);
                    break;
            }
            deltaTime = getRuntime() - initTime;
            if (Math.abs(error) < 30) {
                i += Ki * Math.abs(error) * deltaTime;
            }
            if (i > 0.3) {
                i = 0.3;
            }
            d = (Kd*(Math.abs(error)-pre_error));
        }
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        brake(100);
    }

    /**
     * takes in a specfic speed for each individutal motor
     * Mukilan K. 2020.08.07
     *
     * @param rf: right front motor power
     * @param rb: right back motor power
     * @param lf: left front motor power
     * @param lb: left back motor power
     */
    public void powDrive(double rf, double rb, double lf,  double lb){
        rightFront.setPower(rf);
        leftFront.setPower(lf);
        rightBack.setPower(rb);
        leftBack.setPower(lb);
    }

    public void brake(int time){
        powDrive(0.0,0.0,0.0,0.0);
        sleep(time);
    }

    /**
     *
     * @param ang, is a double type, angle of movement,
     * @param dist
     * @param pow
     */
    public void driveVector(double ang, double dist, double pow){
        double yDist = dist*Math.sin(Math.toRadians(ang));
        double xDist = dist*Math.cos(Math.toRadians(ang));
        double turn = 0;
        double target = 0;

        //wheel diameter is 4in so circumfrecne is around 12in, gear ratio is 3:1
        //might want to make that a public variable later
        double rfRot = (yDist - xDist + turn)/36;
        double rbRot = (yDist + xDist + turn)/36;
        double lfRot = (-yDist - xDist + turn)/36;
        double lbRot = (-yDist + xDist + turn)/36;

        int rfTics = (int)(rfRot*TICKS);
        int rbTics = (int)(rbRot*TICKS);
        int lfTics = (int)(lfRot*TICKS);
        int lbTics = (int)(lbRot*TICKS);

        rfPow = (rfTics*(pow))/(double)Math.abs(rfTics);
        rbPow = (rbTics*(pow))/(double)Math.abs(rbTics);
        lfPow = (lfTics*(pow))/(double)Math.abs(lfTics);
        lbPow = (lbTics*(pow))/(double)Math.abs(lbTics);


        //for every 1 motor ratation, the wheel rotates 3 times, thus traveling 36 inches or 3 feet
        //the turn is going to be found form the acceleration vector and with calculating orientation
        //this is not a field centric code just yet, we would do that in a loop and then recalculate the power each time with an offest.
        //with this we will also be able to move in circles by just changing the angles as time/distance changes

        // double rfval = pow*Math.sin(ang+45) - pow*Math.cos(ang+45) + turn;
        // double rbcal = pow*Math.sin(ang+45);
        // double lfval = -pow*Math.sin(ang-45);
        // double lbval = -pow*Math.sin(ang-45)
        /*
        I do not need to calcualte the power, I will just calcualte te distance and roations and base wheel motion based on that

        double rfval = pow*(y - x);
        double rbval = pow*(y + x);
        double lfval = pow*(-y - x);
        double lbval = pow*(-y + x);
        */

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        double initAng = Math.abs(angles.firstAngle);
        //int tic = (dist/(wheel_diameter))*TICKS;
        //NOTE: rev = dist/(wheel_diameter)

        telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                + " target: " + String.format("%d", rfTics));
        telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                + " target: "+String.format("%d", rbTics));
        telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                + " target: "+String.format("%d", lfTics));
        telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                + " target: "+String.format("%d", lbTics));
        telemetry.addData("5", "intial angle: " + Double.toString(initAng));
        telemetry.update();

        angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
        initAng = Math.abs(angles.firstAngle);

        rightFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        rightBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftFront.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        leftBack.setMode(DcMotor.RunMode.STOP_AND_RESET_ENCODER);

        rightFront.setTargetPosition(rfTics);
        rightBack.setTargetPosition(rbTics);
        leftFront.setTargetPosition(lfTics);
        leftBack.setTargetPosition(lbTics);

        rfPos = rightFront.getCurrentPosition();
        rbPos = rightBack.getCurrentPosition();
        lfPos = leftFront.getCurrentPosition();
        lbPos = leftBack.getCurrentPosition();

        rightFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        rightBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftFront.setMode(DcMotor.RunMode.RUN_TO_POSITION);
        leftBack.setMode(DcMotor.RunMode.RUN_TO_POSITION);

        //while loop must use || because when using && if a revolution of 0 is given to one motor, the statement
        //short ciruits and stops, but the || allows to run each motor at individual revolutions
        while((Math.abs(rfPos) < Math.abs(rfTics)) || (Math.abs(rbPos) < Math.abs(rbTics))||
                (Math.abs(lfPos) < Math.abs(lfTics))|| (Math.abs(lbPos) < Math.abs(lbTics))){

            // implement constatant drift correction/ collision compensation using the IMU values
            //use this same principal for diagonal movemtn, and also use encoder values to see if the tics move, and reserse to compensate
            //for unnecceray movement
            angles = imu.getAngularOrientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES);
            double curAng = angles.firstAngle;

            //initTime = getRuntime();
            //double target = 0;
            double error = curAng - target;
            double correction = (Kp*error);

            rfPos = rightFront.getCurrentPosition();
            rbPos = rightBack.getCurrentPosition();
            lfPos = leftFront.getCurrentPosition();
            lbPos = leftBack.getCurrentPosition();

            if((Math.abs(rfPos) != Math.abs(rfTics))){
                rfScalPow = ((Math.abs(rfTics)-Math.abs(rfPos))/(Math.abs((Math.abs(rfTics)-Math.abs(rfPos))))) *
                        (Range.clip(rfPow*((double)Math.abs(rfTics)-(double)(Math.abs(rfPos))/(double)Math.abs(rfTics)),-pow,pow)-correction);
            }else{ rfScalPow = 0.0; }

            if((Math.abs(rbPos) != Math.abs(rbTics))){
                rbScalPow = ((Math.abs(rbTics)-Math.abs(rbPos))/(Math.abs((Math.abs(rbTics)-Math.abs(rbPos))))) *
                        (Range.clip(rbPow*((double)Math.abs(rbTics)-(double)(Math.abs(rbPos))/(double)Math.abs(rbTics)),-pow,pow)-correction);
            }else{ rbScalPow = 0.0; }

            if((Math.abs(lfPos) != Math.abs(lfTics))){
                lfScalPow = ((Math.abs(lfTics)-Math.abs(lfPos))/(Math.abs((Math.abs(lfTics)-Math.abs(lfPos))))) *
                        (Range.clip(lfPow*((double)Math.abs(lfTics)-(double)(Math.abs(lfPos))/(double)Math.abs(lfTics)),-pow,pow)-correction);
            }else{ lfScalPow = 0.0; }

            if((Math.abs(lbPos) != Math.abs(lbTics))){
                lbScalPow = ((Math.abs(lbTics)-Math.abs(lbPos))/(Math.abs((Math.abs(lbTics)-Math.abs(lbPos))))) *
                        (Range.clip(lbPow*((double)Math.abs(lbTics)-(double)(Math.abs(lbPos))/(double)Math.abs(lbTics)),-pow,pow)-correction);
            }else{ lbScalPow = 0.0; }

            powDrive(rfScalPow,rbScalPow,lfScalPow,lbScalPow);

            telemetry.addData("1", "motorRightFront: " + String.format("%d", rightFront.getCurrentPosition())
                    + " target: " + String.format("%d", rfTics)
                    + " power: " + Double.toString(Math.round(rfScalPow*100)/100.0)
                    + " raw power: " + Double.toString(Math.round(rfPow*100)/100.0));
            telemetry.addData("2", "motorRightFront: " + String.format("%d", rightBack.getCurrentPosition())
                    + " target: "+String.format("%d", rbTics)
                    + " power: " + Double.toString(Math.round(rbScalPow*100)/100.0)
                    + " raw power: " + Double.toString(Math.round(rbPow*100)/100.0));
            telemetry.addData("3", "motorRightFront: " + String.format("%d", leftFront.getCurrentPosition())
                    + " target: "+String.format("%d", lfTics)
                    + " power: " + Double.toString(Math.round(lfScalPow*100)/100.0)
                    + " raw power: " + Double.toString(Math.round(lfPow*100)/100.0));
            telemetry.addData("4", "motorRightFront: " + String.format("%d", leftBack.getCurrentPosition())
                    + " target: "+String.format("%d", lbTics)
                    + " power: " + Double.toString(Math.round(lbScalPow*100)/100.0)
                    + " raw power: " + Double.toString(Math.round(lbPow*100)/100.0));
            telemetry.addData("5", "intial angle: " + Double.toString(initAng) + "current angle: " + Double.toString(curAng));
            telemetry.addData("6", "error: " + Double.toString(error));
            telemetry.addData("7", "correction: " + Double.toString(correction));
            telemetry.update();
        }
        //need to reset the mode of the motors so that it can work with other methods such as powDrive
        rightFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        rightBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftFront.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
        leftBack.setMode(DcMotor.RunMode.RUN_USING_ENCODER);

        //stops setting power to the motors after the target position has been reached
        //beffore it used to just stop because the loop was exited
        brake(100);
    }

    /**
     drivePolar method takes in an input of distance, angle, and boolean mainatin orentaion(mainOr)
     if mainOr is true, then robot does not turn, if false robot turns and moves forward
     NOTE: since its holonomic, assign an integer to each side of the robot (easer to work with in the future
     depending on attachments and use a waterfall conditional)
     */

    /*
    placeholer drive method
    */
    public void teLogic(double X_move,double Y_move){
        double Omega1 = X_move + Y_move;//rf
        double Omega2 = X_move - Y_move;//lf
        double Omega3 = X_move + Y_move;//lb
        double Omega4 = X_move - Y_move;//rb
    }

    /*
    the folowing driveMethod is for cartesian coordinates
    The logic used in this command to determine the power assigned to each motor is the
    same as the logic for the teleOp code and matches input ranges form controller

    NOTE: Program the TeleOp to be field centric for best use
     */
    public void driveXY(int x, int y, double turn) {
        //calculations for the power to be applied to each wheel
        // NOTE: these are what would be used for the telOP inputs form controller
        double leftFrontVal = y + turn + x;
        double leftBackVal = y + turn - x;
        double rightFrontVal = y - turn - x;
        double rightBackVal = y - turn + x;

        leftFront.setPower(leftFrontVal);
        rightFront.setPower(rightFrontVal);
        leftBack.setPower(leftBackVal);
        rightBack.setPower(rightBackVal);
    }
}