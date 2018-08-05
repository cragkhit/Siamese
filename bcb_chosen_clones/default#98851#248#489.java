    private byte[] buildKMZ(String url, double begin, double end, double intv, int cnt, String imgname) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        StringBuffer sb;
        String kmls;
        String places;
        String daes;
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        sdf.setTimeZone(new java.util.SimpleTimeZone(0, "UTC"));
        try {
            String line;
            sb = new StringBuffer();
            java.io.File file = new java.io.File(kmlFileName);
            java.io.FileReader fr = new java.io.FileReader(file);
            java.io.BufferedReader br = new java.io.BufferedReader(fr);
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            kmls = sb.toString();
            sb = new StringBuffer();
            file = new java.io.File(placemarkFileName);
            fr = new java.io.FileReader(file);
            br = new java.io.BufferedReader(fr);
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            places = sb.toString();
            sb = new StringBuffer();
            file = new java.io.File(daeFileName);
            fr = new java.io.FileReader(file);
            br = new java.io.BufferedReader(fr);
            while ((line = br.readLine()) != null) sb.append(line).append('\n');
            daes = sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        String metaname = imgname.substring(0, imgname.lastIndexOf('.')) + ".xml";
        System.err.println("image channel: " + imgname);
        System.err.println("metadata channel: " + metaname);
        ChannelMap cm = new ChannelMap();
        ChannelMap cmin = null;
        try {
            cm.Add(imgname);
            cm.Add(metaname);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            String[] daedoc = new String[cnt];
            byte[][] image = new byte[cnt][];
            String[] stime = new String[cnt];
            if (cnt > 1) intv = (end - begin) / (2 * (cnt - 1));
            System.err.println("cnt " + cnt + ", intv " + intv);
            double lastetime = begin;
            double btime = begin;
            double etime = begin + intv;
            double lastitime = 0;
            for (int i = 0; i < cnt; i++) {
                double rtime = begin + 2 * i * intv;
                sink.Request(cm, rtime, 0, "absolute");
                cmin = sink.Fetch(4000);
                int imgidx = cmin.GetIndex(imgname);
                boolean writekml = true;
                if (imgidx > -1) {
                    image[i] = cmin.GetDataAsByteArray(cmin.GetIndex(imgname))[0];
                    System.err.println("i=" + i + ", imagelength=" + image[i].length);
                    if (i < cnt - 1 && cmin.GetTimeStart(imgidx) - lastitime < 1e-3) {
                        System.err.println("duplicate image!!");
                        writekml = false;
                    } else {
                        lastetime = etime;
                        lastitime = cmin.GetTimeStart(imgidx);
                    }
                } else {
                    System.err.println("NO IMAGE!!");
                    writekml = false;
                }
                if (writekml) {
                    btime = lastetime;
                    if (i == 0) btime = begin;
                    etime = begin + (2 * i + 1) * intv;
                    if (i == cnt - 1) etime = end;
                    System.err.println("btime " + (btime * 1000));
                    System.err.println("etime " + (etime * 1000));
                    String bdate = sdf.format(new java.util.Date((long) (btime * 1000)));
                    String edate = sdf.format(new java.util.Date((long) (etime * 1000)));
                    System.err.println("bdate " + bdate);
                    System.err.println("edate " + edate);
                    stime[i] = (new Long(Math.round(rtime * 10000))).toString();
                    byte[] metastring = null;
                    int idx = cmin.GetIndex(metaname);
                    if (idx > -1) {
                        if (cmin.GetType(idx) == ChannelMap.TYPE_BYTEARRAY) {
                            metastring = cmin.GetDataAsByteArray(idx)[0];
                        } else if (cmin.GetType(idx) == ChannelMap.TYPE_STRING) {
                            metastring = (cmin.GetDataAsString(idx)[0]).getBytes();
                        }
                        System.err.println("metastring: " + metastring);
                    } else {
                        try {
                            java.io.File metafile = new java.io.File(metaname.substring(1 + metaname.lastIndexOf('/')));
                            sb = new StringBuffer();
                            String line = null;
                            java.io.FileReader fr = new java.io.FileReader(metafile);
                            java.io.BufferedReader br = new java.io.BufferedReader(fr);
                            while ((line = br.readLine()) != null) sb.append(line).append('\n');
                            metastring = sb.toString().getBytes();
                        } catch (Exception e) {
                            System.err.println("NO METADATA!!");
                            e.printStackTrace();
                        }
                    }
                    String docname;
                    String llongitude, llatitude, laltitude, lrange, ltilt, lheading;
                    String clongitude, clatitude, caltitude, croll, ctilt, cheading, czoom;
                    String idistance, iheight, iaspect, izoffset, izskew;
                    double LLx, LLy, LLz, URx, URy, URz, LRx, LRy, LRz, ULx, ULy, ULz;
                    try {
                        javax.xml.parsers.DocumentBuilder db = javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
                        java.io.ByteArrayInputStream meta = new java.io.ByteArrayInputStream(metastring);
                        org.w3c.dom.Element e = db.parse(meta).getDocumentElement();
                        org.w3c.dom.Element ee = (org.w3c.dom.Element) e.getElementsByTagName("name").item(0);
                        docname = ee.getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("LookAt").item(0);
                        llongitude = ee.getElementsByTagName("longitude").item(0).getFirstChild().getNodeValue();
                        llatitude = ee.getElementsByTagName("latitude").item(0).getFirstChild().getNodeValue();
                        laltitude = ee.getElementsByTagName("altitude").item(0).getFirstChild().getNodeValue();
                        lrange = ee.getElementsByTagName("range").item(0).getFirstChild().getNodeValue();
                        ltilt = ee.getElementsByTagName("tilt").item(0).getFirstChild().getNodeValue();
                        lheading = ee.getElementsByTagName("heading").item(0).getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("Camera").item(0);
                        String ccoordinates = ee.getElementsByTagName("coordinates").item(0).getFirstChild().getNodeValue();
                        String[] coord = ccoordinates.split(",");
                        clongitude = coord[0];
                        clatitude = coord[1];
                        caltitude = coord[2];
                        croll = ee.getElementsByTagName("roll").item(0).getFirstChild().getNodeValue();
                        ctilt = ee.getElementsByTagName("tilt").item(0).getFirstChild().getNodeValue();
                        cheading = ee.getElementsByTagName("heading").item(0).getFirstChild().getNodeValue();
                        czoom = ee.getElementsByTagName("zoom").item(0).getFirstChild().getNodeValue();
                        ee = (org.w3c.dom.Element) e.getElementsByTagName("Image").item(0);
                        idistance = ee.getElementsByTagName("distance").item(0).getFirstChild().getNodeValue();
                        iheight = ee.getElementsByTagName("height").item(0).getFirstChild().getNodeValue();
                        iaspect = ee.getElementsByTagName("aspect").item(0).getFirstChild().getNodeValue();
                        izoffset = ee.getElementsByTagName("zoffset").item(0).getFirstChild().getNodeValue();
                        izskew = ee.getElementsByTagName("zskew").item(0).getFirstChild().getNodeValue();
                        double d = Double.parseDouble(idistance);
                        double a = Double.parseDouble(iaspect);
                        double o = Double.parseDouble(izoffset);
                        double h = Double.parseDouble(iheight);
                        double Arad = Math.toRadians(Double.parseDouble(izskew));
                        double sinA = Math.sin(Arad);
                        double cosA = Math.cos(Arad);
                        double w = h * a / 2;
                        LLx = -w * cosA;
                        LLy = d - w * sinA;
                        LLz = -h / 2 + o;
                        URx = w * cosA;
                        URy = d + w * sinA;
                        URz = h / 2 + o;
                        LRx = w * cosA;
                        LRy = d + w * sinA;
                        LRz = -h / 2 + o;
                        ULx = -w * cosA;
                        ULy = d - w * sinA;
                        ULz = h / 2 + o;
                    } catch (Exception e) {
                        System.err.println("Exception parsing metadata!");
                        e.printStackTrace();
                        return null;
                    }
                    if (i == 0) {
                        kmls = kmls.replaceFirst("##docname##", docname);
                        kmls = kmls.replaceFirst("##llongitude##", llongitude);
                        kmls = kmls.replaceFirst("##llatitude##", llatitude);
                        kmls = kmls.replaceFirst("##lrange##", lrange);
                        kmls = kmls.replaceFirst("##ltilt##", ltilt);
                        kmls = kmls.replaceFirst("##lheading##", lheading);
                    }
                    daedoc[i] = daes.replaceFirst("##href-jpg##", "i" + stime[i] + ".jpg");
                    daedoc[i] = daedoc[i].replaceFirst("##LLx##", Double.toString(LLx));
                    daedoc[i] = daedoc[i].replaceFirst("##LLy##", Double.toString(LLy));
                    daedoc[i] = daedoc[i].replaceFirst("##LLz##", Double.toString(LLz));
                    daedoc[i] = daedoc[i].replaceFirst("##URx##", Double.toString(URx));
                    daedoc[i] = daedoc[i].replaceFirst("##URy##", Double.toString(URy));
                    daedoc[i] = daedoc[i].replaceFirst("##URz##", Double.toString(URz));
                    daedoc[i] = daedoc[i].replaceFirst("##LRx##", Double.toString(LRx));
                    daedoc[i] = daedoc[i].replaceFirst("##LRy##", Double.toString(LRy));
                    daedoc[i] = daedoc[i].replaceFirst("##LRz##", Double.toString(LRz));
                    daedoc[i] = daedoc[i].replaceFirst("##ULx##", Double.toString(ULx));
                    daedoc[i] = daedoc[i].replaceFirst("##ULy##", Double.toString(ULy));
                    daedoc[i] = daedoc[i].replaceFirst("##ULz##", Double.toString(ULz));
                    String pm = places.replaceFirst("##id##", "pm" + i);
                    pm = pm.replaceFirst("##name##", "d" + stime[i] + ".dae");
                    pm = pm.replaceFirst("##begin##", bdate);
                    pm = pm.replaceFirst("##end##", edate);
                    pm = pm.replaceFirst("##llongitude##", llongitude);
                    pm = pm.replaceFirst("##llatitude##", llatitude);
                    pm = pm.replaceFirst("##lrange##", lrange);
                    pm = pm.replaceFirst("##ltilt##", ltilt);
                    pm = pm.replaceFirst("##lheading##", lheading);
                    pm = pm.replaceFirst("##clongitude##", clongitude);
                    pm = pm.replaceFirst("##caltitude##", caltitude);
                    pm = pm.replaceFirst("##clatitude##", clatitude);
                    pm = pm.replaceFirst("##ctilt##", ctilt);
                    pm = pm.replaceFirst("##croll##", croll);
                    pm = pm.replaceFirst("##cheading##", cheading);
                    pm = pm.replaceAll("##zoom##", czoom);
                    pm = pm.replaceFirst("##href-dae##", "d" + stime[i] + ".dae");
                    kmls = kmls.replaceFirst("##placemark##", pm + "##placemark##");
                }
            }
            kmls = kmls.replaceFirst("##placemark##", "");
            ZipOutputStream zos = new ZipOutputStream(baos);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(Deflater.DEFAULT_COMPRESSION);
            ZipEntry ze = new ZipEntry("foo.kml");
            zos.putNextEntry(ze);
            byte[] data = kmls.getBytes();
            zos.write(data, 0, data.length);
            zos.closeEntry();
            for (int i = 0; i < cnt; i++) {
                if (daedoc[i] != null) {
                    ze = new ZipEntry("d" + stime[i] + ".dae");
                    zos.putNextEntry(ze);
                    data = daedoc[i].getBytes();
                    zos.write(data, 0, data.length);
                    zos.closeEntry();
                }
            }
            for (int i = 0; i < cnt; i++) {
                if (stime[i] != null) {
                    ze = new ZipEntry("i" + stime[i] + ".jpg");
                    zos.putNextEntry(ze);
                    data = image[i];
                    zos.write(data, 0, data.length);
                    zos.closeEntry();
                }
            }
            zos.close();
        } catch (Exception e) {
            System.err.println("Exception zipping kml/dae documents");
            e.printStackTrace();
        }
        return baos.toByteArray();
    }
