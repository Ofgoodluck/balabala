package lab.mars.sim.core.missileInterception;

import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import lab.mars.sim.core.missileInterception.models.Radar.ElectricityEnvironment;
import lab.mars.sim.core.missileInterception.models.Radar.Radar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by imrwz on 2017/5/4.
 */
public class SimpleRadarCalculationAgent {

    private static float CalculatePositiveRadarGt(Radar radar, float theta) {
        float theta0_5 = radar.GetTheta0_5();
        float part = (float) (-ElectricityEnvironment.K * Math.pow(2 * theta / theta0_5, 2));
        float ret = (float) Math.exp(part);

        return ret;
    }

    private static Vector3 CalculateCoordinates(float longEdge, float theta) {
        float x = (float) (longEdge * Math.sin(theta * Math.PI / 180));
        float y = (float) (longEdge * Math.cos(theta * Math.PI / 180));
        return new Vector3(x, y, 0);
    }

    private static Vector3 CalculateSpin(Vector3 original, float spinAngle, Vector3 related) {
        Quaternion q = new Quaternion();
        q.x = 0;
        q.y = 0;
        q.z = (float) Math.sin((spinAngle / 2.0f) * (Math.PI / 180f));
        q.w = (float) Math.cos((spinAngle / 2.0f) * (Math.PI / 180f));
        return (q.transform(original)).add(related);

    }

    private static Vector3 CalculateSpin(Vector3 original, float spinAngle) {
        return CalculateSpin(original, spinAngle, Vector3.Zero);
    }

    public static void RefreshRadarSpin(Radar radar, float timeStep) {
        float spinSpeed = radar.GetScanAngularVelocity();
        float spinRange = radar.GetMaxScanAngle();
        boolean isCounterClockwise = radar.GetCurrentScanIsCounterClockwise();
        float actualSpinSpeed = spinSpeed * timeStep;
        if (isCounterClockwise) {
            radar.SetCurrentScanAngle(radar.GetCurrentScanAngle() + actualSpinSpeed);
            if (radar.GetCurrentScanAngle() > spinRange) {
                radar.SetCurrentScanIsCounterClockwise(false);
            }
        } else {
            radar.SetCurrentScanAngle(radar.GetCurrentScanAngle() - actualSpinSpeed);
            if (radar.GetCurrentScanAngle() < 0) {
                radar.SetCurrentScanIsCounterClockwise(true);
            }
        }
    }

    public static List<Vector3> CalculatePositiveRadarEffectiveAreaWithoutJam
            (Radar radar, float targetRCS) {
        float Pt = radar.GetPt();
        float lambda = radar.GetLambda();
        List<Vector3> _radarBorderArray = new ArrayList<>();
        List<Vector3> rightPart = new ArrayList<>();
        List<Vector3> leftPart = new ArrayList<>();
        for (float theta = 0.0f; theta < 90; theta += ElectricityEnvironment.deltaTheta) {
            float part = (float) ((Pt * CalculatePositiveRadarGt(radar, theta) *
                    Math.pow(lambda, 2) * targetRCS) / (Math.pow(4 * Math.PI, 3) *
                    ElectricityEnvironment.K * ElectricityEnvironment.L));
            float Rmax = (float) (Math.pow(part, 0.25f));
            //Debug.Log("Calculate: {" + theta + "," + Rmax + "}");
            //float radarRotatedAngle = radar.GetBindedObject().rotation.getAngleAround(ElectricityEnvironment.radarOriginalOrientation);

            Vector3 right = CalculateCoordinates(Rmax, /*radarRotatedAngle + */ theta);
            //float currentScanAngle = radar.GetCurrentScanAngle();

            Vector3 left = new Vector3(-right.x, right.y, right.z);//CalculateSpin(new Vector3(-original.x, original.y, original.z)
            //, currentScanAngle);

            rightPart.add(right);
            leftPart.add(left);
        }

        Collections.reverse(leftPart);
        //leftPart.Reverse();
        _radarBorderArray.addAll(rightPart);
        _radarBorderArray.addAll(leftPart);
        //RefreshRadarSpin(radar);
        return _radarBorderArray;
    }


}
