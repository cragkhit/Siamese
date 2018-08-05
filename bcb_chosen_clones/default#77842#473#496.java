    void runFile(String runcomfile, String file) {
        try {
            String s = null;
            Runtime r = Runtime.getRuntime();
            Properties p = System.getProperties();
            Process ps = null;
            ps = r.exec(runcomfile + " " + file);
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(ps.getInputStream()));
            BufferedReader stdOutput1 = new BufferedReader(new InputStreamReader(ps.getErrorStream()));
            while ((s = stdInput.readLine()) != null) {
                System.out.println(s);
                if ((s = stdInput.readLine()) == null) {
                    System.out.println("not running...");
                }
            }
            while ((s = stdOutput1.readLine()) != null) {
                System.out.println(s);
            }
            stdInput.close();
            stdOutput1.close();
        } catch (IOException e) {
            warnme("Error open file " + e);
        }
    }
