package edu.mit.dlab.ppganalyzer;

/**
 * Calculates the derivative of points on graph
 */
public class FivePointDerivative {

    double twoBack = 0;
    double oneBack = 0;
    double middle = 0;
    double oneAhead = 0;
    double twoAhead = 0;
    double h = 1;
    boolean useDerivative = false;
    double scaleFactor=.05;

    public FivePointDerivative(double h) {
        this.h = h;
    }

    int d(int newValue) {
        if (!useDerivative) {
            return newValue;
        }
        this.twoBack = this.oneBack;
        this.oneBack = this.middle;
        this.middle = this.oneAhead;
        this.oneAhead = this.twoAhead;
        this.twoAhead = newValue;
        double top = this.twoBack - 8 * this.oneBack + 8 * this.oneAhead - this.twoAhead;
        if (twoBack==0||twoBack==0||middle==0||oneAhead==0||twoAhead==0){
            return newValue;
        }
        int result=(int) (top / (scaleFactor*this.h));
        return result;
    }
}
