package com.maiyoule.miniexam.ui.template;

import java.util.List;

import com.maiyoule.miniexam.model.AnswersModel;

import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

public class QuestionAnalysisTemplate implements TemplateMethodModelEx {

    public Object exec(List args) throws TemplateModelException {

        Object idstr = args.get(0);
        if (idstr == null) {
            return "0";
        }

        int id = Integer.parseInt(idstr.toString());
        //查询数量
        List<AnswersModel> answers = AnswersModel.dao.findByCache("answer", "answer_" + id,
                String.format("select count(id) as al from answers where question_id=%d", id));
        AnswersModel answer = answers.get(0);
        int allcount = answer.getInt("al");
        if (allcount < 1) {
            allcount = 1;
        }
        List<AnswersModel> ranswers = AnswersModel.dao
                .findByCache(
                        "answer",
                        "answer_right_" + id,
                        String.format(
                                "select count(id) as al from answers where question_id=%d and status=1",
                                id));
        AnswersModel ranswer = ranswers.get(0);
        int rcount = ranswer.getInt("al");
        return String.format("%.2f", (rcount * 100 / (allcount + 0.0)));

    }

}
