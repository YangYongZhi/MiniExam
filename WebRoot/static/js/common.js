
function jAlert(content,title,callback){
	$.dialog({
		"content":content,
		"title":typeof(title)!='undefined'?title:"提示",
	"ok":typeof(callback)=='function'?callback:function(){}
	});
}

function jConfirm(content,title,ok,cancel){
	$.dialog({
		"content":content,
		"title":title,
		"ok":typeof(ok)=='function'?ok:function(){},
		"cancel":typeof(cancel)=='function'?cancel:function(){}
	});
}