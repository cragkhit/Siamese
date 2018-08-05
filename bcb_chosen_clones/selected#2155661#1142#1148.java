    private double searchMax(Function func, Function deriv, double x1, double x2, int depth) {
        double mid = (x1 + x2) / 2;
        param[0] = mid;
        if (depth >= 13) return func.getVal(param);
        double slope = deriv.getVal(param);
        if (slope > 0) return searchMax(func, deriv, mid, x2, depth + 1); else return searchMax(func, deriv, x1, mid, depth + 1);
    }
