/*
 * Copyright 2006 by Heiner Jostkleigrewe
 * Diese Datei steht unter LGPL - siehe beigefügte lpgl.txt
 */
package de.jost_net.OBanToo.Test;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import de.jost_net.OBanToo.QIF.QIFDateiWriter;

public class QIFWriterTest
{

  public static void main(String[] args) throws IOException
  {
    FileOutputStream fos = new FileOutputStream("test.qif");
    QIFDateiWriter qifw = new QIFDateiWriter(fos);
    qifw.setBetrag(100.01);
    qifw.setDatum(new Date());
    qifw.setEmpfaenger("Fritzchen Müller");
    qifw.addAdresse("Testenhausen");
    qifw.write();

    qifw.setBetrag(200);
    qifw.setDatum(new Date());
    qifw.setEmpfaenger("Franz Sepplhuber");
    qifw.setKategorie("Reisen");
    qifw.write();

    qifw.close();
  }
}
