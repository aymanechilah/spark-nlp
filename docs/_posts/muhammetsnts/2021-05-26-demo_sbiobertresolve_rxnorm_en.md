---
layout: model
title: Sentence Entity Resolver for RxNorm (demo_sbiobertresolve_rxnorm)
author: John Snow Labs
name: demo_sbiobertresolve_rxnorm
date: 2021-05-26
tags: [entity_resolution, clinical, licensed, en]
task: Entity Resolution
language: en
edition: Spark NLP for Healthcare 3.0.3
spark_version: 3.0
supported: true
deprecated: true
article_header:
  type: cover
use_language_switcher: "Python-Scala-Java"
---

## Description

This model runs on Spark NLP for Heathcare v3.1.0 and after. The model maps extracted medical entities to RxNorm codes using chunk embeddings, and has faster load time, with a speedup of about 6X when compared to previous versions. Also the load process now is more memory friendly meaning that the maximum memory required during load time is smaller, reducing the chances of OOM exceptions, and thus relaxing hardware requirements.

## Predicted Entities

RxNorm Codes and their normalized definition with `sbiobert_base_cased_mli ` embeddings.

{:.btn-box}
<button class="button button-orange" disabled>Live Demo</button>
<button class="button button-orange" disabled>Open in Colab</button>
[Download](https://s3.amazonaws.com/auxdata.johnsnowlabs.com/clinical/models/demo_sbiobertresolve_rxnorm_en_3.0.3_3.0_1622046366845.zip){:.button.button-orange.button-orange-trans.arr.button-icon}

## How to use



<div class="tabs-box" markdown="1">
{% include programmingLanguageSelectScalaPythonNLU.html %}
```python
...
chunk2doc = Chunk2Doc().setInputCols("ner_chunk").setOutputCol("ner_chunk_doc")
 
sbert_embedder = BertSentenceEmbeddings\
     .pretrained("sbiobert_base_cased_mli","en","clinical/models")\
     .setInputCols(["ner_chunk_doc"])\
     .setOutputCol("sbert_embeddings")
 
rxnorm_resolver = SentenceEntityResolverModel.pretrained("demo_sbiobertresolve_rxnorm","en", "clinical/models") \
     .setInputCols(["ner_chunk", "sbert_embeddings"]) \
     .setOutputCol("resolution")\
     .setDistanceFunction("EUCLIDEAN")

nlpPipeline = Pipeline(stages=[document_assembler, sentence_detector, tokenizer, word_embeddings, clinical_ner, ner_converter, chunk2doc, sbert_embedder, rxnorm_resolver])

data = spark.createDataFrame([["This is an 82 - year-old male with a history of prior tobacco use , hypertension , chronic renal insufficiency , COPD , gastritis , and TIA who initially presented to Braintree with a non-ST elevation MI and Guaiac positive stools , transferred to St . Margaret\'s Center for Women & Infants for cardiac catheterization with PTCA to mid LAD lesion complicated by hypotension and bradycardia requiring Atropine , IV fluids and transient dopamine possibly secondary to vagal reaction , subsequently transferred to CCU for close monitoring , hemodynamically stable at the time of admission to the CCU ."]]).toDF("text")

results = nlpPipeline.fit(data).transform(data)
```
```scala
...
val chunk2doc = Chunk2Doc().setInputCols("ner_chunk").setOutputCol("ner_chunk_doc")
 
val sbert_embedder = BertSentenceEmbeddings
     .pretrained("sbiobert_base_cased_mli","en","clinical/models")
     .setInputCols(Array("ner_chunk_doc"))
     .setOutputCol("sbert_embeddings")
 
val rxnorm_resolver = SentenceEntityResolverModel
     .pretrained("demo_sbiobertresolve_rxnorm","en", "clinical/models")
     .setInputCols(Array("ner_chunk", "sbert_embeddings"))
     .setOutputCol("resolution")
     .setDistanceFunction("EUCLIDEAN")

val pipeline = new Pipeline().setStages(Array(document_assembler, sentence_detector, tokenizer, word_embeddings, clinical_ner, ner_converter, chunk2doc, sbert_embedder, rxnorm_resolver))

val data = Seq.empty["This is an 82 - year-old male with a history of prior tobacco use , hypertension , chronic renal insufficiency , COPD , gastritis , and TIA who initially presented to Braintree with a non-ST elevation MI and Guaiac positive stools , transferred to St . Margaret\'s Center for Women & Infants for cardiac catheterization with PTCA to mid LAD lesion complicated by hypotension and bradycardia requiring Atropine , IV fluids and transient dopamine possibly secondary to vagal reaction , subsequently transferred to CCU for close monitoring , hemodynamically stable at the time of admission to the CCU ."].toDS.toDF("text")

val result = pipeline.fit(data).transform(data)
```
</div>

## Results

```bash
+--------------------+-----+---+---------+-------+----------+-----------------------------------------------+--------------------+
|               chunk|begin|end|   entity|   code|confidence|                                    resolutions|               codes|
+--------------------+-----+---+---------+-------+----------+-----------------------------------------------+--------------------+
|        hypertension|   68| 79|  PROBLEM| 386165|    0.1567|hypercal:::hypersed:::hypertears:::hyperstat...|386165:::217667::...|
|chronic renal ins...|   83|109|  PROBLEM| 218689|    0.1036|nephro calci:::dialysis solutions:::creatini...|218689:::3310:::2...|
|                COPD|  113|116|  PROBLEM|1539999|    0.1644|broncomar dm:::acne medication:::carbon mono...|1539999:::214981:...|
|           gastritis|  120|128|  PROBLEM| 225965|    0.1983|gastroflux:::gastroflux oral product:::uceri...|225965:::1176661:...|
|                 TIA|  136|138|  PROBLEM|1089812|    0.0625|thera tears:::thiotepa injection:::nature's ...|1089812:::1660003...|
|a non-ST elevatio...|  182|202|  PROBLEM| 218767|    0.1007|non-aspirin pm:::aspirin-free:::non aspirin ...|218767:::215440::...|
|Guaiac positive s...|  208|229|  PROBLEM|1294361|    0.0820|anusol rectal product:::anusol hc rectal pro...|1294361:::1166715...|
|cardiac catheteri...|  295|317|     TEST| 385247|    0.1566|cardiacap:::cardiology pack:::cardizem:::car...|385247:::545063::...|
|                PTCA|  324|327|TREATMENT|   8410|    0.0867|alteplase:::reteplase:::pancuronium:::tripe ...|8410:::76895:::78...|
|      mid LAD lesion|  332|345|  PROBLEM| 151672|    0.0549|dulcolax:::lazerformalyde:::linaclotide:::du...|151672:::217985::...|
+--------------------+-----+---+---------+-------+----------+-----------------------------------------------+--------------------+
```

{:.model-param}
## Model Information

{:.table-model}
|---|---|
|Model Name:|demo_sbiobertresolve_rxnorm|
|Compatibility:|Spark NLP for Healthcare 3.0.3+|
|License:|Licensed|
|Edition:|Official|
|Input Labels:|[ner_chunk, drugs_sbert_embeddings]|
|Output Labels:|[rxnorm_code]|
|Language:|en|
|Case sensitive:|false|