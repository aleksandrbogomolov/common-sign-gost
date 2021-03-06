package ru.i_novus.common.sign.app;

/*-
 * -----------------------------------------------------------------
 * common-sign-gost
 * -----------------------------------------------------------------
 * Copyright (C) 2018 - 2019 I-Novus LLC
 * -----------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * -----------------------------------------------------------------
 */

import org.apache.commons.cli.*;
import ru.i_novus.common.sign.api.SignAlgorithmType;
import ru.i_novus.common.sign.util.CryptoIO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class PemToP12Converter {
    public static void main(String... args) throws ParseException, IOException {
        PemToP12Converter pemToP12Converter = new PemToP12Converter();
        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(pemToP12Converter.getOptions(), args);

        String certificateFileName = cmd.getOptionValue("certPemFile");
        String pkFileName = cmd.getOptionValue("privateKeyPemFile");
        String p12FileName = cmd.getOptionValue("p12File");
        String p12Password = cmd.getOptionValue("p12Password");

        Path certificateFile = Paths.get(certificateFileName);
        pemToP12Converter.checkFile(certificateFile);

        Path pkFile = Paths.get(pkFileName);
        pemToP12Converter.checkFile(pkFile);

        Path p12Path = Paths.get(p12FileName);
        if (p12Path.toFile().exists()) {
            Files.delete(p12Path);
        }

        CryptoIO cryptoIO = CryptoIO.getInstance();
        X509Certificate certificate = cryptoIO.readCertFromPEM(certificateFile);
        PrivateKey privateKey = cryptoIO.readPkFromPEM(pkFile, SignAlgorithmType.findByCertificate(certificate));

        cryptoIO.createPkcs12File(p12Path, p12Password, privateKey, new X509Certificate[] { certificate });
    }

    private void checkFile(Path path) {
        File file = path.toFile();
        if (!file.exists()) {
            throw new IllegalArgumentException("Файл '" + path + "' не существует");
        }
        if (!file.isFile()) {
            throw new IllegalArgumentException("'" + path + "' не является файлом");
        }
        if (!Files.isReadable(path)) {
            throw new IllegalArgumentException("Не удается прочитать содержимое файла '" + path + "'. Нет доступа");
        }
    }

    private Options getOptions() {
        Options options = new Options();
        options.addOption("certPemFile", true, "Путь к файлу сертификата в формате PEM");
        options.addOption("privateKeyPemFile", true, "Путь к файлу закрытого ключа в формате PEM");
        options.addOption("p12File", true, "Путь к файлу создаваемого ключевого контейнера");
        options.addOption("p12Password", true, "Пароль ключевого контейнера");
        return options;
    }
}
