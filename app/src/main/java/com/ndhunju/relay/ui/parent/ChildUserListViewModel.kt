package com.ndhunju.relay.ui.parent

import androidx.lifecycle.ViewModel
import com.ndhunju.relay.ui.mockChildUsers

class ChildUserListViewModel: ViewModel() {

    var childUsers = mockChildUsers

    /**
     * User click on [childUsers]
     */
    fun onClickChildUser(childUser: Child) {

    }
}