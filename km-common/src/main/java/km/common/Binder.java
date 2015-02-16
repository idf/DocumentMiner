package km.common;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import java.io.File;
import java.net.URISyntaxException;

/**
 * User: Danyang
 * Date: 2/16/2015
 * Time: 17:47
 */
public class Binder {
    public static void main(String[] args) throws URISyntaxException {
        try {
            File file = new File("settings.xml");
            System.out.println(file.getAbsoluteFile());
            JAXBContext jaxbContext = JAXBContext.newInstance(Settings.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            jaxbMarshaller.marshal(new Settings(), file);
            jaxbMarshaller.marshal(new Settings(), System.out);

        } catch (JAXBException e) {
            e.printStackTrace();
        }

    }
}
