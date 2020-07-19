package com.github.mygreen.sqltemplate;

import java.io.IOException;
import java.util.Optional;

import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.StringUtils;

public class TemplateLoader {

    /**
     * リソースを指定して読み込む。
     * @param resource リソース
     * @param encoding 文字コード
     * @return SQLファイルの内容
     * @throws TwoWaySqlException テンプレートの読み込みに失敗した場合にスローされます。
     */
    public String loadByResource(final Resource resource, final String encoding) {
        try {
            return SqlUtils.readStream(resource.getInputStream(), encoding);
        } catch(IOException e) {
            throw new TwoWaySqlException("Fail load resource", e);
        }
    }

    /**
     * リソースパスを指定して読み込む。
     * {@literal suffixName} が指定されている場合、接尾語付きのリソースを優先して読み込みます。
     *
     * @param location リロースパス
     * @param resourceLoader リロースローダー。
     * @param encoding 文字コード
     * @param suffixName リロースの接尾語
     * @return SQLファイルの内容
     * @throws TwoWaySqlException テンプレートの読み込みに失敗した場合にスローされます。
     */
    public String loadByLocation(final String location,
            final ResourceLoader resourceLoader, final String encoding, final Optional<String> suffixName) {

        if(suffixName.isPresent()) {
            // 方言付きパスの読み込み
            String dialectPath = convertLocationWithDialect(location, suffixName.get());
            try {
                Resource resource = resourceLoader.getResource(dialectPath);
                if(resource.isReadable()) {
                    return SqlUtils.readStream(resource.getInputStream(), encoding);
                }
            } catch(IOException e) {
                // 読み込めない場合
                throw new TwoWaySqlException(String.format("Fail load file : %s", dialectPath), e);

            }
        }

        // 元々のパスでの読み込み
        try {
            Resource resource = resourceLoader.getResource(location);
            if(resource.isReadable()) {
                return SqlUtils.readStream(resource.getInputStream(), encoding);
            }

        } catch(IOException e) {
            // 読み込めない場合
            throw new TwoWaySqlException(String.format("Fail load file : %s", location), e);

        }

        // 読み込み対象のファイルが見つからない場合
        throw new TwoWaySqlException(String.format("Not found or non-readable file : %s", location));

    }

    /**
     * SQLのパスを方言名付きのSQLに変換する。
     * @param location 変換対象のパス
     * @param suffixName リロースの接尾語
     * @return 変換したパス
     */
    private String convertLocationWithDialect(final String location, final String suffixName) {

        final StringBuilder sb = new StringBuilder(location.length());

        final String extension = StringUtils.getFilenameExtension(location);

        // 拡張子を除いたファイル名に方言名を付ける
        sb.append(StringUtils.stripFilenameExtension(location))
            .append("-").append(suffixName);

        // 拡張子がある場合は戻す。
        if(!StringUtils.isEmpty(extension)) {
            sb.append(".").append(extension);
        }

        return sb.toString();

    }
}
