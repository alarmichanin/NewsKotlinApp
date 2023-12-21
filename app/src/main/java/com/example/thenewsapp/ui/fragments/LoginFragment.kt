package com.example.thenewsapp.ui.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewClient
import androidx.navigation.fragment.navArgs
import com.example.thenewsapp.R
import com.example.thenewsapp.databinding.FragmentArticleBinding
import com.example.thenewsapp.ui.NewsActivity
import com.example.thenewsapp.ui.NewsViewModel
import com.google.android.material.snackbar.Snackbar

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var newsViewModel: NewsViewModel
//    private lateinit var binding: FragmentProfileBinding

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//        binding = FragmentProfileBinding.bind(view)

        newsViewModel = (activity as NewsActivity).newsViewModel

        checkUserAuthentication()
    }

    private fun checkUserAuthentication() {
//        if (!newsViewModel.isUserLoggedIn()) {
//            navigateToLogin()
//        } else {
//            setupUserProfile()
//        }
    }

    private fun navigateToLogin() {
        // Перенаправление на экран логина/регистрации
//        findNavController().navigate(R.id.action_profileFragment_to_loginFragment)
    }

//    private fun setupUserProfile() {
//        // Получение данных пользователя
//        val userData = newsViewModel.getUserData()
//
//        // Отображение данных пользователя
//        displayUserData(userData)
//
//        // Настройка слушателя для кнопки редактирования профиля
//        setupEditProfileListener()
//    }

//    private fun displayUserData(userData: UserData) {
//        // Пример отображения имени пользователя
//        binding.profileNameTextView.text = userData.name
//
//        // Здесь можно добавить отображение другой информации пользователя
//    }

//    private fun setupEditProfileListener() {
//        binding.editProfileButton.setOnClickListener {
//            // Обработчик нажатия на кнопку редактирования профиля
//            openEditProfile()
//        }
//    }

    private fun openEditProfile() {
        // Перенаправление на фрагмент редактирования профиля или открытие диалогового окна
        // Например:
        // findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
    }

}