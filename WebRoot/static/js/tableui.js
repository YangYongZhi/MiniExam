/*
* scrollAnimate 0.1
* Copyright (c) 2013 E��  http://fuzhongkuo.com/
* Date: 2013��11��13�� 13:58:36
* ʹ��tableUi ����ʵ�ֱ����б�ɫ������ƶ������л�����Ч������ǿ�û����飡
*/
(function ($) {
    //�����б�ɫ
    $.fn.extend({
        "tableUi": function (options) {
            var defaults = {
                odd: "odd",       /* ż������ʽ*/
                even: "even",     /* ��������ʽ*/
                selected: "tr-bg" /* ѡ������ʽ*/
            };
            var options = $.extend(defaults, options);
            return this.each(function () {
            	$(this).children("tbody").children("tr:odd").addClass(options.odd);
            	$(this).children("tbody").children("tr:even").addClass(options.even);
            	$(this).children("tbody").children("tr").mousemove(function () {
                    $(this).addClass(options.selected);
                }).mouseout(function () {
                    $(this).removeClass(options.selected);
                });
            });
        }
    });
})(jQuery);