/*
 * Copyright 2017-2022 John Snow Labs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.johnsnowlabs.nlp.annotators.sda.pragmatic

import com.johnsnowlabs.nlp.annotators.common.TokenizedSentence

import org.slf4j.LoggerFactory

/**
 * Scorer is a rule based implementation inspired on http://fjavieralba.com/basic-sentiment-analysis-with-python.html
 * Its strategy is to tag words by a dictionary in a sentence context, and later identify such context to get amplifiers
 */
class PragmaticScorer(
                       sentimentDict: Map[String, String],
                       POSITIVE_VALUE: Double = 1.0,
                       NEGATIVE_VALUE: Double = -1.0,
                       INCREMENT_MULTIPLIER: Double = 2.0,
                       DECREMENT_MULTIPLIER: Double = -2.0,
                       REVERT_MULTIPLIER: Double = -1.0
                     ) extends Serializable {

  private val logger = LoggerFactory.getLogger("PragmaticScorer")

  /**
   * Internal class to summarize dictionary key information
   *
   * @param fullKey   represents the whole dictionary key which can be a phrase
   * @param keyHead   represents the first word of the phrase, to be issued for matching
   * @param keyLength represents the amount of words in a phrase key
   * @param sentiment holds the tag for this phrase
   */
  private case class ProcessedKey(fullKey: String, keyHead: String, keyLength: Int, sentiment: String)

  /** Hardcoded tags for this implementation */
  private val POSITIVE = "positive"
  private val NEGATIVE = "negative"
  private val INCREMENT = "increment"
  private val DECREMENT = "decrement"
  private val REVERT = "revert"

  /** reads the dictionary and processes it into useful information on a [[ProcessedKey]] */
  private val processedKeys: Array[ProcessedKey] = {
    sentimentDict.map { case (key, sentiment) =>
      val keySplits = key.split(" ").map(_.trim)
      ProcessedKey(key, keySplits.head, keySplits.length, sentiment)
    }.toArray
  }

  /**
   * scores lowercased words by their headers within a sentence context. Sets the tag for every word
   *
   * @param tokenizedSentences POS tagged sentence. POS tags are not really useful in this implementation for now.
   * @return
   */
  def score(tokenizedSentences: Array[TokenizedSentence]): Double = {
    val sentenceSentiments: Array[Array[String]] = tokenizedSentences.map(tokenizedSentence => {
      tokenizedSentence.tokens.flatMap(token => {
        val targetWord = token.toLowerCase
        val targetWordPosition = tokenizedSentence.tokens.indexOf(token)
        processedKeys.find(processedKey => {
          /** takes the phrase based on the dictionary key phrase length to check if it matches the entire phrase */
          targetWord == processedKey.keyHead &&
            tokenizedSentence.tokens
              .slice(targetWordPosition, targetWordPosition + processedKey.keyLength)
              .mkString(" ") == processedKey.fullKey
        }).map(processedKey => {
          /** if it exists, put the appropiate tag of the full phrase */
          logger.debug(s"matched sentiment ${processedKey.fullKey} as ${processedKey.sentiment}")
          sentimentDict(processedKey.fullKey)
        })
      })
    })
    /** score is returned based on config tweaked parameters */
    val sentimentBaseScore: Array[(Array[String], Double)] = sentenceSentiments.map(sentiment => (
      sentiment,
      sentiment.foldRight(0.0)((sentiment, score) => {
        sentiment match {
          case POSITIVE => score + POSITIVE_VALUE
          case NEGATIVE => score + NEGATIVE_VALUE
          case _ => score
        }
      })
    ))
    logger.debug(s"sentiment positive/negative base score is ${sentimentBaseScore.map(_._2).sum}")

    /** amplifiers alter the base score of positive and negative tags. Sums the entire sentence score */
    sentimentBaseScore.map { case (sentiments, baseScore) => sentiments.foldRight(baseScore)((sentiment, currentScore) => {
      sentiment match {
        case INCREMENT => currentScore * INCREMENT_MULTIPLIER
        case DECREMENT => currentScore * DECREMENT_MULTIPLIER
        case REVERT => currentScore * REVERT_MULTIPLIER
        case _ => currentScore
      }
    })
    }.sum
  }
}