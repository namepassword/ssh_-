$(function() {
	var productId = getQueryString('productId');
	var productUrl = '/gradudes/frontend/listproductdetailpageinfo?productId='
			+ productId;

	$
			.getJSON(
					productUrl,
					function(data) {
						if (data.success) {
							var product = data.product;
							$('#product-img').attr('src',getContextPath()+product.imgAddr);
							$('#product-time').text(
									new Date(product.lastEditTime)
											.Format("yyyy-MM-dd"));
							if(product.point!=undefined){
							$('#product-point').text("购买可得："+product.point+"积分");
							}
							$('#normal-price').text("￥"+product.normalPrice);
							if(product.promotionPrice!=""){
								$('#promotion-price').text("￥"+product.promotionPrice);
								}
							
							
							$('#product-name').text(product.productName);
							$('#product-desc').text(product.productDesc);
							var imgListHtml = '';
							product.productImgList.map(function(item, index) {
								imgListHtml += '<div> <img src="'
									+getContextPath()+ item.imgAddr + '" width="100%"/></div>';
							});
							// 生成购买商品的二维码供商家扫描
							if(data.needQRCode){
								imgListHtml += '<div> <img src="/gradudes/frontend/generateqrcode4product?productId='
									+ product.productId + '"width="100%"/></div>';
							}
							$('#imgList').html(imgListHtml);
						}
					});
	$('#me').click(function() {
		$.openPanel('#panel-left-demo');
	});
	$.init();
});
