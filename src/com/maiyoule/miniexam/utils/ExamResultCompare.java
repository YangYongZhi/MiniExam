package com.maiyoule.miniexam.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;

import com.maiyoule.miniexam.GUIConstants;
import com.maiyoule.miniexam.entity.FlexAnswerItem;
import com.maiyoule.miniexam.entity.FlexQuestion;
import com.maiyoule.miniexam.model.QuestionModel;

public class ExamResultCompare {
    private List<FlexAnswerItem> answers;
    private List<FlexQuestion> an;
    private int scoresingle, scoremuti, scorejudge;
    private Map<Integer, Short> resultlist = new HashMap<Integer, Short>();

    private int score = 0;

    public int genResult(List<FlexAnswerItem> answers, List<FlexQuestion> an, int scoresingle,
            int scoremuti, int scorejudge) {
        this.answers = answers;
        this.an = an;
        this.scoresingle = scoresingle;
        this.scoremuti = scoremuti;
        this.scorejudge = scorejudge;
        return this._gen();
    }

    private int _gen() {
        resultlist.clear();
        for (int i = 0; i < this.answers.size(); i++) {
            FlexAnswerItem fai = this.answers.get(i);
            FlexQuestion fq = this._findQuestion(fai.getId());
            if (fai == null || fai.getValue() == null) {
                continue;
            }
            if (fq == null || fq.getValue() == null || fq.getType() == null) {
                continue;
            }
            /*
             * 计算单选题得分
             */
            if (fq.getType().equals(QuestionModel.TYPE_SINGLE)) {
                if (fq.getValue().equals(fai.getValue())) {
                    //答案正确
                    this.score += this.scoresingle;

                    this.resultlist.put(fq.getId(), (short) 1);
                } else {
                    this.resultlist.put(fq.getId(), (short) 0);
                }
            } else if (fq.getType().equals(QuestionModel.TYPE_MUTI)) {
                /*
                 * 计算多选题得分
                 */
                String[] useranswers = StringUtils.split(fai.getValue(), ";");
                String[] trueanswers = StringUtils.split(fq.getValue(), GUIConstants.STRING_SPLITE);
                boolean isanswertrue = true;
                if (useranswers.length < trueanswers.length) {
                    isanswertrue = false;
                } else {
                    for (String tmpuseranswer : useranswers) {
                        if (!ArrayUtils.contains(trueanswers, tmpuseranswer)) {
                            isanswertrue = false;
                            break;
                        }
                    }
                }
                if (isanswertrue) {
                    this.score += this.scoremuti;
                    this.resultlist.put(fq.getId(), (short) 1);
                } else {
                    this.resultlist.put(fq.getId(), (short) 0);
                }
            } else if (fq.getType().equals(QuestionModel.TYPE_JUDGE)) {
                /*
                 * 计算判断题得分
                 */
                String userAnswer = fai.getValue(); //为空表示未作答，直接不计分
                if (StringHelper.isNullOrEmpty(userAnswer)) {
                    this.resultlist.put(fq.getId(), (short) 0);
                } else {
                    String checkstr = userAnswer.equals("yes") ? "1" : "0";
                    if (fq.getValue().equals(checkstr)) {
                        //答案正确
                        this.score += this.scorejudge;

                        this.resultlist.put(fq.getId(), (short) 1);
                    } else {
                        this.resultlist.put(fq.getId(), (short) 0);
                    }
                }
            }
        }
        return score;
    }

    private FlexQuestion _findQuestion(int id) {
        for (int i = 0; i < this.an.size(); i++) {
            FlexQuestion fq = this.an.get(i);
            if (fq.getId() == id) {
                return fq;
            }
        }
        return null;
    }

    /**
     * 获取最后一次处理结果
     * 
     * @return
     */
    public Map<Integer, Short> getLastResultList() {
        return this.resultlist;
    }

}
