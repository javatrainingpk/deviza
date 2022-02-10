/**
 * Egyszerû deviza váltó program.
 * @author Petõ Kornél
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

  private static JLabel     label_forint = new JLabel("Átváltandó összeg [Ft]:");
  private static JLabel     label_valuta = new JLabel("Valuta:");
  private static JLabel     label_result = new JLabel("Eredmény [Ft]:");

/**
* A tanúsítványkezelés felüldefiniálása, hogy ne keletkezzen kivétel az online XML betöltés során.
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
* GUI összeállítása és feliratkozás az eseménykezelõkre.
*/
  private valto() {
    this.setDefaultCloseOperation(EXIT_ON_CLOSE);
    this.setTitle("Devizaváltó program");

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

    btnBrowse = new JButton("XML betöltése");
    btnBrowse.setSize(120, 20);
    btnBrowse.setLocation(10, 10);
    btnBrowse.addActionListener(this);
    panel.add(btnBrowse);

    cbUrlOrFile = new JCheckBox("online XML-bõl");
    cbUrlOrFile.setSize(130, 20);
    cbUrlOrFile.setLocation(150, 10);
    cbUrlOrFile.addActionListener(this);
    panel.add(cbUrlOrFile);

    btnExchange = new JButton("Átváltás");
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
* Beállítja vagy tiltja az összeg mezõ szerkeszthetõségét és az átváltás gomb engedélyezettségét.
* @param p_state_in  a beállítandó állapotot jelzõje (TRUE vagy FALSE)
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
* Beállítja az XML betöltés gombjának felugró leírását.
* @param hintState  a jelölõnégyzet bepipált állapota esetén online, egyébként pedig kitallózható állományból történõ XML betöltést jelez
*/
  private void setHint(boolean hintState) {
    if (hintState) {
      btnBrowse.setToolTipText("Online XML betöltése");
    }
    else {
      btnBrowse.setToolTipText("A felolvasandó XML tallózása");
    }
  }

/**
* Visszaad egy BufferedReader objektumot, a beállított opcióknak megfelelõen az online elérhetõ, vagy pedig kitallózott XML-hez.
* @return  a kiválasztott XML olvasható objektuma (vagy sikertelen tallózás, illetve hib aesetén null)
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
      JOptionPane.showMessageDialog(this, "Hiba történt a betöltéskor!", "Hibaüzenet", JOptionPane.ERROR_MESSAGE);
      setState(false);
    }
    return null;
  }

/**
* Elvégzi a betöltött és kiválasztott adatok alapján a tényleges átváltást, vagy ha nincsen megadva érték, akkor üríti az eredmény mezõt.
*/
  private void exchange() {
    if (text_forint.getText().trim().length() == 0) {
      text_result.setText("");
      //JOptionPane.showMessageDialog(this, "Nincsen átváltandó érték!", "Hibaüzenet", JOptionPane.ERROR_MESSAGE);
    }
    else {
      text_result.setText( String.valueOf(Float.valueOf(text_forint.getText()) / Float.valueOf(comboValuta.getSelectedItem().toString().substring(comboValuta.getSelectedItem().toString().indexOf("-") + 1)) * ftRate) );
    }
  }

/**
* Eseménykezelõ az XML betöltés, átváltás és jelölõnégyzet állítása esetén elvégzendõ feladatok megvalósításához.
* @param ae  a keletkezett esemény forrása
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
        JOptionPane.showMessageDialog(this, "Hiba történt a betöltéskor!", "Hibaüzenet", JOptionPane.ERROR_MESSAGE);
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
* Fõ programrész.
* @param args  paraméterek (nem kezeljük õket)
*/
  public static void main(String[] args) {
    new valto();
  }
}