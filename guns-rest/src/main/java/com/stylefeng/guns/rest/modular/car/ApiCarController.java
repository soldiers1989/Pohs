package com.stylefeng.guns.rest.modular.car;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.md.cart.constant.CartStatus;
import com.md.cart.model.Cart;
import com.md.cart.model.CartItem;
import com.md.cart.oe.CartObject;
import com.md.cart.oe.GoodObject;
import com.md.cart.oe.ShopObject;
import com.md.cart.service.ICartItemService;
import com.md.cart.service.ICartService;
import com.md.goods.model.Goods;
import com.md.goods.model.PriceTag;
import com.md.goods.model.Product;
import com.md.goods.model.UploadFile;
import com.md.goods.service.IGoodsService;
import com.md.goods.service.IPriceTagService;
import com.md.goods.service.IProductService;
import com.md.goods.service.IShopService;
import com.md.goods.service.IUploadFileService;
import com.stylefeng.guns.core.base.controller.BaseController;
import com.stylefeng.guns.core.util.ToolUtil;
import com.stylefeng.guns.rest.modular.car.dto.CartRequest;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/car")
public class ApiCarController extends BaseController {

	@Resource
	ICartService cartService;

	@Resource
	ICartItemService cartItemService;

	@Resource
	IPriceTagService priceTagService;

	@Resource
	IShopService shopService;

	@Resource
	IGoodsService goodsService;

	@Resource
	IProductService productService;

	@Resource
	IUploadFileService uploadFileService;
	
	@ApiOperation(value = "加入购物车", notes = "加入购物车")
	@RequestMapping(value = "/addCart", method = RequestMethod.POST)
	public ResponseEntity<?> addCart( @RequestBody CartRequest cartRequest) {
		Cart cart = cartService.addSave(cartRequest.getMemberId()); // 初始化购物车
		PriceTag tag = priceTagService.findByShopAndProduct(cartRequest.getProductId(), cartRequest.getShopId());
		if(ToolUtil.isNotEmpty(tag)) {
			CartItem cartItem = cartItemService.findByTagId(tag.getId(), cart.getId());
			if(cartItem != null) {
				cartItem.setQuantity(cartItem.getQuantity() + cartRequest.getQuantity());
				cartItemService.updateById(cartItem); // 新增购物车项
			}else {
				CartItem cartItem2 = new CartItem();
				cartItem2.setCartId(cart.getId());
				cartItem2.setProductId(cartRequest.getProductId());
				cartItem2.setPriceTagId(tag.getId());
				cartItem2.setQuantity(cartRequest.getQuantity());
				cartItem2.setShopId(cartRequest.getShopId());
				cartItem2.setStatus(CartStatus.YES.getCode());
				cartItemService.add(cartItem2); // 新增购物车项
				if(ToolUtil.isEmpty(cart.getQuantity())) {
					cart.setQuantity(1);
				}else {
					cart.setQuantity(cart.getQuantity() + 1);
				}
				cartService.updateById(cart); // 加入购物车
			}
		}else {
			return ResponseEntity.ok("不存在当前商品");
		}
		return ResponseEntity.ok("加入购物车成功");
	}

	@ApiOperation(value = "批量加入购物车", notes = "批量加入购物车")
	@RequestMapping(value = "/addCartMore", method = RequestMethod.POST)
	public ResponseEntity<?> addCartMore( @RequestBody CartRequest cartRequest) {
		Cart cart = cartService.addSave(cartRequest.getMemberId()); // 初始化购物车
		String[] productArray = cartRequest.getProducts().split(",");
		String[] shopArray = cartRequest.getShopIds().split(",");
		String[] quantityArray = cartRequest.getQuantitys().split(",");
		if(productArray.length > 0) {
			for(Integer i = 0;i<productArray.length;i++) {
				PriceTag tag = priceTagService.findByShopAndProduct(Long.valueOf(productArray[i]), Long.valueOf(shopArray[i]));
				if(ToolUtil.isNotEmpty(tag)) {
					CartItem cartItem = cartItemService.findByTagId(tag.getId(), cart.getId());
					if(cartItem != null) {
						cartItem.setQuantity(cartItem.getQuantity() + Integer.valueOf(quantityArray[i]));
						cartItemService.updateById(cartItem); // 新增购物车项
					}else {
						CartItem cartItem2 = new CartItem();
						cartItem2.setCartId(cart.getId());
						cartItem2.setProductId(Long.valueOf(productArray[i]));
						cartItem2.setPriceTagId(tag.getId());
						cartItem2.setQuantity(Integer.valueOf(quantityArray[i]));
						cartItem2.setShopId(Long.valueOf(shopArray[i]));
						cartItem2.setStatus(CartStatus.YES.getCode());
						cartItemService.add(cartItem2); // 新增购物车项
						if(ToolUtil.isEmpty(cart.getQuantity())) {
							cart.setQuantity(1);
						}else {
							cart.setQuantity(cart.getQuantity() + 1);
						}
						cartService.updateById(cart); // 加入购物车
					}
				}else {
					return ResponseEntity.ok("不存在当前商品");
				}
			}
		}else {
			return ResponseEntity.ok("加入购物车成功");
		}
		return ResponseEntity.ok("加入购物车成功");
	}
	
	@ApiOperation(value = "修改购物车规格", notes = "修改购物车规格")
	@RequestMapping(value = "/modifyCart", method = RequestMethod.POST)
	public ResponseEntity<?> modifyCart(
			@ApiParam("购物车项id") @RequestParam(value = "cartItemId", required = true) @RequestBody long cartItemId,
			@ApiParam("产品id") @RequestParam(value = "productId", required = true) @RequestBody long productId,
			@ApiParam("数量") @RequestParam(value = "quantity", required = true) @RequestBody Integer quantity) {
		CartItem item = cartItemService.selectById(cartItemId);
		PriceTag tag = priceTagService.findByShopAndProduct(productId, item.getShopId());
		item.setPriceTagId(tag.getId());
		item.setProductId(productId);
		item.setQuantity(quantity);
		cartItemService.updateById(item);
		return ResponseEntity.ok("购物车修改成功");
	}

	@ApiOperation(value = "修改购物车数量", notes = "修改购物车数量")
	@RequestMapping(value = "/modifyCartQuan", method = RequestMethod.POST)
	public ResponseEntity<?> modifyCartQuan( @RequestBody CartRequest cartRequest) {
		CartItem item = cartItemService.selectById(cartRequest.getCartItemId());
		item.setQuantity(cartRequest.getQuantity());
		cartItemService.updateById(item);
		return ResponseEntity.ok("购物车修改成功");
	}

	@ApiOperation(value = "购物车列表", notes = "购物车列表")
	@RequestMapping(value = "/getCartList", method = RequestMethod.POST)
	public ResponseEntity<?> getCartList( @RequestBody CartRequest cartRequest) {
		CartObject cartOb = new CartObject();
		List<ShopObject> shopList = new ArrayList<>();
		Cart cart = cartService.findById(cartRequest.getMemberId());
		if (cart != null) {
			List<Long> shopResult = cartItemService.getShopListByCartId(cart.getId());
			if (shopResult.size() > 0) {
				for (int i = 0; i < shopResult.size(); i++) {
					List<GoodObject> goodList = new ArrayList<>();
					ShopObject shopItems = new ShopObject();
					shopItems.setShopid(shopResult.get(i));
					shopItems.setShopName(shopService.findById(shopResult.get(i)).getName());
					List<CartItem> itemResult = cartItemService.findByCartShop(cart.getId(), shopResult.get(i));
					if (itemResult.size() > 0) {
						for (CartItem item : itemResult) {
							GoodObject goodItems = new GoodObject();
							PriceTag tag = priceTagService.findByShopAndProduct(item.getProductId(), item.getShopId());
							if(ToolUtil.isNotEmpty(tag)) {
								if(ToolUtil.isNotEmpty(tag.getPrice())) {
									goodItems.setPrice(tag.getPrice());
								}
								goodItems.setGoodsId(tag.getGoodsId());
								Goods goods = goodsService.findById(tag.getGoodsId());
								if(ToolUtil.isNotEmpty(goods)) {
									goodItems.setGoodsName(goods.getName());
									goodItems.setGoodsSn(goods.getSn());
								}
							}
							goodItems.setCartItemId(item.getId());
							Product product = productService.findById(item.getProductId());
							if(ToolUtil.isNotEmpty(product)) {
								goodItems.setSkuName(product.getName());
								UploadFile upload =  uploadFileService.getById(product.getImage());
								if(ToolUtil.isNotEmpty(upload)) {
									goodItems.setImageUrl(upload.getUrl());
								}
							}
							goodItems.setProductId(item.getProductId());
							goodItems.setQuantity(item.getQuantity());
							goodItems.setStatus(item.getStatus());
							goodList.add(goodItems);
						}
					}
					shopItems.setGoodItems(goodList);
					shopList.add(shopItems);
				}
				cartOb.setShopItems(shopList);
				cartOb.setCartId(cart.getId());
				cartOb.setMemberId(cart.getMemberId());
				cartOb.setQuantity(cart.getQuantity());
				return ResponseEntity.ok(cartOb);
			} else {
				return ResponseEntity.ok(cartOb);
			}
		} else {
			return ResponseEntity.ok(cartOb);
		}
	}
	
	@ApiOperation(value = "删除购物车", notes = "删除购物车")
	@RequestMapping(value = "/deleteCar", method = RequestMethod.POST)
	public ResponseEntity<?> deleteCar( @RequestBody CartRequest cartRequest) {
		String[] arr = cartRequest.getItemIds().split(",");
		for(int i = 0; i < arr.length; i++) {
			cartItemService.deleteById(Long.valueOf(arr[i]));
		}
		Cart cart = cartService.selectById(cartRequest.getCartId());
		cart.setQuantity(cart.getQuantity() - arr.length);
		cartService.updateById(cart);
		return ResponseEntity.ok("删除成功");
	}
}
