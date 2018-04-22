    public int binarySearch(final char key) {
        int low = 0;
        int middle;
        int high = super.size() - 1;
        char middleValue;
        while (low <= high) {
            middle = (low + high) / 2;
            middleValue = this.charArray[middle];
            if (middleValue < key) {
                low = middle + 1;
            } else if (middleValue > key) {
                high = middle - 1;
            } else {
                return middle;
            }
        }
        return -(low + 1);
    }