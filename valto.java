/**
 * Egyszer� deviza v�lt� program.
 * @author Pet� Korn�l
 */
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class valto extends JFrame implements ActionListener {

  private static File       inputFile;
  private static float      ftRate;

  private static JPanel     panel;
  private static JButton    btnBrowse, btnExchange;
  private static JCheckBox  cbUrlOrFile;
  private static JTextField text_forint, text_result;
  private static JComboBox  comboValuta;

  private static JLabel     label_forint = new JLabel("�tv�ltand� �sszeg [Ft]:");
  private static JLabel     label_valuta = new JLabel("Valuta:");
  private static JLabel     label_result = new JLabel("Eredm�ny [Ft]:");

/**
* A tan�s�tv�nykezel�s fel�ldefini�l�sa, hogy ne keletkezzen kiv�tel az online XML bet�lt�s sor�n.
*/
  public class CertificatValidator {

    public CertificatValidator() {

      TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }
          public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
          public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
            }
        }
      };
      try {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, trustAllCerts, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
      }
      catch (Exception e) {
        }
    }
  }

/**
* GUI �ssze�ll�t�sa �s feliratkoz�s az esem�nykezel�kre.
*/
  private valto() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setTitle("Devizav�lt� program");

    Container cp = getContentPane();
    cp.setLayout(null);
    setBounds(200, 200, 430, 255);
    setResizable(false);

    panel = new JPanel();
    panel.setLayout(null);
    panel.setSize(400, 203);
    panel.setLocation(7, 7);
    panel.setBorder(BorderFactory.createEtchedBorder());
    cp.add(panel);

    label_forint.setSize(150, 20);
    label_forint.setLocation(10, 45);
    panel.add(label_forint);

    label_valuta.setSize(100, 20);
    label_valuta.setLocation(10, 80);
    panel.add(label_valuta);

    label_result.setSize(100, 20);
    label_result.setLocation(10, 155);
    panel.add(label_result);

    btnBrowse = new JButton("XML bet�lt�se");
    btnBrowse.setSize(120, 20);
    btnBrowse.setLocation(10, 10);
    btnBrowse.addActionListener(this);
    panel.add(btnBrowse);

    cbUrlOrFile = new JCheckBox("online XML-b�l");
    cbUrlOrFile.setSize(130, 20);
    cbUrlOrFile.setLocation(150, 10);
    cbUrlOrFile.addActionListener(this);
    panel.add(cbUrlOrFile);

    btnExchange = new JButton("�tv�lt�s");
    btnExchange.setSize(90, 20);
    btnExchange.setLocation(10, 120);
    btnExchange.addActionListener(this);
    btnExchange.setEnabled(false);
    panel.add(btnExchange);

    text_forint = new JTextField("");
    text_forint.setSize(240, 20);
    text_forint.setLocation(150, 45);
    text_forint.setHorizontalAlignment(JTextField.RIGHT);
    text_forint.setEditable(false);
    panel.add(text_forint);

    text_result = new JTextField("");
    text_result.setSize(240, 20);
    text_result.setLocation(150, 155);
    text_forint.setHorizontalAlignment(JTextField.RIGHT);
    text_result.setEditable(false);
    panel.add(text_result);

    comboValuta = new JComboBox();
    comboValuta.setSize(240, 20);
    comboValuta.setLocation(155, 90);
    cp.add(comboValuta);

    setHint(cbUrlOrFile.isSelected());
    this.setVisible(true);
    this.setEnabled(true);
  }

/**
* Be�ll�tja vagy tiltja az �sszeg mez� szerkeszthet�s�g�t �s az �tv�lt�s gomb enged�lyezetts�g�t.
* @param p_state_in  a be�ll�tand� �llapotot jelz�je (TRUE vagy FALSE)
*/
  private void setState(boolean p_state_in) {
    text_forint.setEditable(p_state_in);
    btnExchange.setEnabled(p_state_in);
    if (!p_state_in) {
      comboValuta.removeAllItems();
    }
    exchange();
  }

/**
* Be�ll�tja az XML bet�lt�s gombj�nak felugr� le�r�s�t.
* @param hintState  a jel�l�n�gyzet bepip�lt �llapota eset�n online, egy�bk�nt pedig kitall�zhat� �llom�nyb�l t�rt�n� XML bet�lt�st jelez
*/
  private void setHint(boolean hintState) {
    if (hintState) {
      btnBrowse.setToolTipText("Online XML bet�lt�se");
    }
    else {
      btnBrowse.setToolTipText("A felolvasand� XML tall�z�sa");
    }
  }

/**
* Visszaad egy BufferedReader objektumot, a be�ll�tott opci�knak megfelel�en az online el�rhet�, vagy pedig kitall�zott XML-hez.
* @return  a kiv�lasztott XML olvashat� objektuma (vagy sikertelen tall�z�s, illetve hib aeset�n null)
* @throws MalformedURLException
* @throws IOException 
*/
  private BufferedReader readerOpen() throws MalformedURLException, IOException {
    try {
      if (cbUrlOrFile.isSelected()) {
        new CertificatValidator();
        URL urlFile = new URL("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml");
        InputStream inputStream = urlFile.openStream();
        InputStreamReader fr = new InputStreamReader(inputStream);
        BufferedReader    br = new BufferedReader(fr);
        return br;
      }
      else {
        JFileChooser loadPanel = new JFileChooser();
        loadPanel.showOpenDialog(this);
        inputFile = loadPanel.getSelectedFile();
        if (inputFile != null) {
          FileReader     fr = new FileReader(inputFile);
          BufferedReader br = new BufferedReader(fr);
          return br;
        }
      }
    }
    catch (Exception exc) {
      JOptionPane.showMessageDialog(this, "Hiba t�rt�nt a bet�lt�skor!", "Hiba�zenet", JOptionPane.ERROR_MESSAGE);
      setState(false);
    }
    return null;
  }

/**
* Elv�gzi a bet�lt�tt �s kiv�lasztott adatok alapj�n a t�nyleges �tv�lt�st, vagy ha nincsen megadva �rt�k, akkor �r�ti az eredm�ny mez�t.
*/
  private void exchange() {
    if (text_forint.getText().trim().length() == 0) {
      text_result.setText("");
      //JOptionPane.showMessageDialog(this, "Nincsen �tv�ltand� �rt�k!", "Hiba�zenet", JOptionPane.ERROR_MESSAGE);
    }
    else {
      text_result.setText( String.valueOf(Float.valueOf(text_forint.getText()) / Float.valueOf(comboValuta.getSelectedItem().toString().substring(comboValuta.getSelectedItem().toString().indexOf("-") + 1)) * ftRate) );
    }
  }

/**
* Esem�nykezel� az XML bet�lt�s, �tv�lt�s �s jel�l�n�gyzet �ll�t�sa eset�n elv�gzend� feladatok megval�s�t�s�hoz.
* @param ae  a keletkezett esem�ny forr�sa
*/
  public void actionPerformed(ActionEvent ae) {
    if (ae.getSource() == btnBrowse) {
      try {
        BufferedReader bReader = readerOpen();
        if (bReader != null) {
          comboValuta.removeAllItems();
          String currencyType;
          String currencyRate;
          for(String lineStr; (lineStr = bReader.readLine()) != null; ) {
            lineStr = lineStr.trim();
            if (lineStr.contains("<Cube currency=") &&
                lineStr.contains("rate=")) {
              currencyType = lineStr.substring(16, 19);
              currencyRate = lineStr.substring(lineStr.indexOf("rate=") + 6, lineStr.indexOf("/>") - 1);
              comboValuta.addItem(currencyType + " - " + currencyRate);

              if (currencyType.compareTo("HUF") == 0) {
                ftRate = Float.valueOf(currencyRate);
              }
            }
          }
          setState(true);
        }
      }
      catch (Exception exc) {
        JOptionPane.showMessageDialog(this, "Hiba t�rt�nt a bet�lt�skor!", "Hiba�zenet", JOptionPane.ERROR_MESSAGE);
        exc.printStackTrace();
        setState(false);
      }
    }
    if (ae.getSource() == btnExchange) {
      exchange();
    }
    if (ae.getSource() == cbUrlOrFile) {
      setHint(cbUrlOrFile.isSelected());
    }
  }

/**
* F� programr�sz.
* @param args  param�terek (nem kezelj�k �ket)
*/
  public static void main(String[] args) {
    new valto();
  }
}