package com.example.barberbooking.Interface;

import com.example.barberbooking.Database.CartItem;

import java.util.List;

public interface ICartItemLoadListener {
    void onGetAllItemCartLoadSuccess(List<CartItem> cartItemList);
}
