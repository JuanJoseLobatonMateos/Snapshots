package com.jlobatonm.snapshots

import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jlobatonm.snapshots.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() , MainAux
{
    
    private lateinit var mBinding: ActivityMainBinding
    
    private lateinit var mActiveFragment: Fragment
    private var mFragmentManager: FragmentManager? = null
    
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null
    
    private val authResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.main_auth_welcome, Toast.LENGTH_SHORT).show()
            } else {
                if (IdpResponse.fromResultIntent(it.data) == null) {
                    finish()
                }
            }
        }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        
        setupAuth()
    }
    
    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                authResult.launch(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            listOf(
                                AuthUI.IdpConfig.EmailBuilder().build(), // Email
                                AuthUI.IdpConfig.GoogleBuilder().build() // Google
                            )
                        )
                        .setTosAndPrivacyPolicyUrls(
                            "https://example.com/terms.html",
                            "https://example.com/privacy.html"
                        )
                        .setAlwaysShowSignInMethodScreen(true) // Mostrar siempre la pantalla de métodos de inicio de sesión
                        .build()
                )
                mFragmentManager = null
            } else {
                SnapshotsApplication.currentUser = it.currentUser!!
                
                val fragmentProfile =
                    mFragmentManager?.findFragmentByTag(ProfileFragment::class.java.name)
                fragmentProfile?.let {
                    // Manejar fragmento existente
                }
                
                if (mFragmentManager == null) {
                    // Inicializar el administrador de fragmentos y cargar el fragmento inicial
                    mFragmentManager = supportFragmentManager
                    setupBottomNav(mFragmentManager!!)
                }
            }
        }
    }
    
    private fun setupBottomNav(fragmentManager: FragmentManager) {
        mFragmentManager?.let { // Limpiar antes para prevenir errores
            for (fragment in it.fragments) {
                it.beginTransaction().remove(fragment!!).commit()
            }
        }
        
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()
        
        mActiveFragment = homeFragment
        
        fragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, profileFragment, ProfileFragment::class.java.name)
            .hide(profileFragment).commit()
        fragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, addFragment, AddFragment::class.java.name)
            .hide(addFragment).commit()
        fragmentManager.beginTransaction()
            .add(R.id.nav_host_fragment, homeFragment, HomeFragment::class.java.name).commit()
        
        mBinding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.action_home -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment)
                        .commit()
                    mActiveFragment = homeFragment
                    true
                }
                
                R.id.action_add -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment)
                        .commit()
                    mActiveFragment = addFragment
                    true
                }
                
                R.id.action_profile -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment)
                        .commit()
                    mActiveFragment = profileFragment
                    true
                }
                
                else -> false
            }
        }
        mBinding.bottomNav.setOnItemReselectedListener {
            when(it.itemId)
            {
                R.id.action_home -> (homeFragment as HomeAux).goToTop()
            }
        }
        // Set default selected item to home
        mBinding.bottomNav.selectedItemId = R.id.action_home
    }
    
    override fun onResume()
    {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }
    
    override fun onPause()
    {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }
    
    /*
    *   MainAux
    * */
    override fun showMessage(resId: Int , duration: Int)
    {
        Snackbar.make(mBinding.root , resId , duration)
            .setAnchorView(mBinding.bottomNav)
            .show()
    }
    
}

    


