/*
* scrollAnimate 0.1
* Copyright (c) 2013 E盘  http://fuzhongkuo.com/
* Date: 2013年11月13日 13:58:36
* 使用tableUi 方便实现表格隔行变色，鼠标移动背景切换交互效果，增强用户体验！
*/
(function ($) {
    //表格隔行变色
    $.fn.extend({
        "tableUi": function (options) {
            var defaults = {
                odd: "odd",       /* 偶数行样式*/
                even: "even",     /* 奇数行样式*/
                selected: "tr-bg" /* 选中行样式*/
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