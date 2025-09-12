package com.oauth2.Util.Elasticsearch.Provider;

import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch.indices.IndexSettings;
import co.elastic.clients.elasticsearch.indices.IndexSettingsAnalysis;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CommonSettingsProvider {
    @Getter
    @Value("${elastic.nGramAnalyzer}")
    private String autoNGramAnalyzer;

    @Getter
    @Value("${elastic.edgeNGramAnalyzer}")
    private String autoEdgeNGramAnalyzer;
    @Value("${elastic.nGram}")
    private String nGram;
    @Value("${elastic.edgeNGram}")
    private String edgeNGram;

    public IndexSettingsAnalysis getAnalysis() {
        NGramTokenizer nGramTokenizer = new NGramTokenizer.Builder()
                // 단어를 최소 1글자에서 4글자 까지 나눔
                // ex) 에어신신파스 -> 에, 어, 신, 신 ..., 에어, 어신 ..., 에어신 어신파 ..., 에어신신 ..., 신신파스
                .minGram(1)
                .maxGram(4)
                .tokenChars(TokenChar.Letter, TokenChar.Digit, TokenChar.Symbol, TokenChar.Punctuation, TokenChar.Whitespace)
                .build();

        TokenizerDefinition tokenizerNGram = new TokenizerDefinition.Builder()
                .ngram(nGramTokenizer)
                .build();

        CustomAnalyzer customNGramAnalyzer = new CustomAnalyzer.Builder()
                .tokenizer(nGram)
                .filter("lowercase")
                .build();

        Analyzer nGramAnalyzer = new Analyzer.Builder()
                .custom(customNGramAnalyzer)
                .build();

        EdgeNGramTokenizer edgeNGramTokenizer = new EdgeNGramTokenizer.Builder()
                // 단어를 최소 1글자에서 4글자 까지 나눔
                // ex) 에어신신파스 -> 에, 어, 신, 신 ..., 에어, 어신 ..., 에어신 어신파 ..., 에어신신 ..., 신신파스
                .minGram(1)
                .maxGram(20)
                .tokenChars(TokenChar.Letter, TokenChar.Digit, TokenChar.Symbol, TokenChar.Punctuation, TokenChar.Whitespace)
                .build();

        TokenizerDefinition tokenizerEdgeNGram = new TokenizerDefinition.Builder()
                .edgeNgram(edgeNGramTokenizer)
                .build();

        CustomAnalyzer customEdgeNGramAnalyzer = new CustomAnalyzer.Builder()
                .tokenizer(edgeNGram)
                .filter("lowercase")
                .build();

        Analyzer edgeNGramAnalyzer = new Analyzer.Builder()
                .custom(customEdgeNGramAnalyzer)
                .build();


        // 분석기 설정
        return new IndexSettingsAnalysis.Builder()
                .analyzer(autoNGramAnalyzer, nGramAnalyzer)
                .analyzer(autoEdgeNGramAnalyzer, edgeNGramAnalyzer)
                .tokenizer(nGram, b -> b.definition(tokenizerNGram))
                .tokenizer(edgeNGram, b -> b.definition(tokenizerEdgeNGram))
                .build();
    }
    public IndexSettings getDefaultSettings() {
        return new IndexSettings.Builder()
                .maxNgramDiff(5)
                .analysis(getAnalysis())
                .build();
    }
}
