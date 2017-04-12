/*
 * To oprogramowanie jest własnością
 *
 * OPI - Ośrodek Przetwarzania Informacji,
 * Al. Niepodległości 188B, 00-608 Warszawa
 * Numer KRS: 0000127372
 * Sąd Rejonowy dla m. st. Warszawy w Warszawie XII Wydział
 * Gospodarczy KRS
 * REGON: 006746090
 * NIP: 525-000-91-40
 * Wszystkie prawa zastrzeżone. To oprogramowanie może być używane tylko
 * zgodnie z przeznaczeniem. OPI nie odpowiada za ewentualne wadliwe
 * działanie kodu.
 *
 * Id: $Id$
 * Data ostatniej modyfikacji: $LastChangedDate$
 * Użytkownik ostatnio modyfikujący: $Author$
 * Rewizja: $Revision$
 */
package pl.sdadas.fsbrowser.utils;

import com.google.common.base.Charsets;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.*;

/**
 * @author Sławomir Dadas
 */
public final class JaxbUtils {

    public static <T> String marshall(T object) {

        try {
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter writer = new StringWriter();
            marshaller.marshal(object, writer);
            return writer.toString();
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> void marshall(T object, File output) {

        FileWriter writer = null;
        try {
            writer = new FileWriter(output);
            JAXBContext context = JAXBContext.newInstance(object.getClass());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(object, writer);
        } catch (JAXBException | IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(writer);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T unmarshall(String xml, Class<T> clazz) {

        if (StringUtils.isBlank(xml)) return null;

        try {
            JAXBContext context = JAXBContext.newInstance(clazz);
            Unmarshaller unmarshaller = context.createUnmarshaller();
            StringReader reader = new StringReader(xml);
            return (T) unmarshaller.unmarshal(reader);
        } catch (JAXBException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T unmarshall(File file, Class<T> clazz) {

        Resource resource = new FileSystemResource(file);
        return unmarshall(resource, clazz);
    }

    public static <T> T unmarshall(Resource resource, Class<T> clazz) {

        if (resource == null) return null;
        if (!resource.isReadable()) return null;

        InputStream is = null;
        try {
            is = resource.getInputStream();
            String xml = IOUtils.toString(is, Charsets.UTF_8);
            return unmarshall(xml, clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    private JaxbUtils() {
    }
}
