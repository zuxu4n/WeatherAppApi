package weatherApp.src;

import javax.swing.SwingUtilities;

public class AppLauncher {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new WeatherAppGui().setVisible(true);

                // System.out.println(appFinal.getLocationData("Tokyo"));

                // System.out.println(appFinal.getCurrentTime());
            }
        });
    }
}
