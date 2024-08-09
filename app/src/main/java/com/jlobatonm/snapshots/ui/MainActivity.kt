package com.jlobatonm.snapshots.ui

import android.os.Bundle
import android.os.DeadObjectException
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.jlobatonm.snapshots.R
import com.jlobatonm.snapshots.SnapshotsApplication
import com.jlobatonm.snapshots.databinding.ActivityMainBinding
import com.jlobatonm.snapshots.ui.fragments.AddFragment
import com.jlobatonm.snapshots.ui.fragments.HomeFragment
import com.jlobatonm.snapshots.ui.fragments.ProfileFragment
import com.jlobatonm.snapshots.utils.HomeAux
import com.jlobatonm.snapshots.utils.MainAux

class MainActivity : AppCompatActivity(), MainAux {

    // Variable de binding para el layout principal
    private lateinit var mBinding: ActivityMainBinding

    // Fragmento activo y administrador de fragmentos
    private lateinit var mActiveFragment: Fragment
    private var mFragmentManager: FragmentManager? = null

    // Listener de autenticación y instancia de FirebaseAuth
    private lateinit var mAuthListener: FirebaseAuth.AuthStateListener
    private var mFirebaseAuth: FirebaseAuth? = null

    // Resultado de la autenticación
    private val authResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RESULT_OK) {
            Toast.makeText(this, R.string.main_auth_welcome, Toast.LENGTH_SHORT).show()
        } else {
            if (IdpResponse.fromResultIntent(it.data) == null) {
                finish()
            }
        }
    }

    // Método onCreate para inicializar la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflar el layout usando view binding
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        try {
            setupAuth()
            // Código que interactúa con el objeto Window
            // Por ejemplo, configuraciones de la ventana o interacciones con el WindowManager
        } catch (e: DeadObjectException) {
            Log.e("MainActivity", "DeadObjectException caught: ${e.message}")
            // Manejo adicional de la excepción, si es necesario
        }
    }

    // Configurar la autenticación de Firebase
    private fun setupAuth() {
        mFirebaseAuth = FirebaseAuth.getInstance()
        mAuthListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser == null) {
                authResult.launch(
                    AuthUI.getInstance().createSignInIntentBuilder()
                        .setIsSmartLockEnabled(false)
                        .setAvailableProviders(
                            listOf(
                                AuthUI.IdpConfig.EmailBuilder().build(),
                                AuthUI.IdpConfig.GoogleBuilder().build()
                            )
                        )
                        .setTosAndPrivacyPolicyUrls(
                            "https://example.com/terms.html",
                            "https://example.com/privacy.html"
                        )
                        .setAlwaysShowSignInMethodScreen(true)
                        .setLogo(R.drawable.logo_app) // Set logo de la app
                        .setTheme(R.style.Theme_Snapshots) // Set theme
                        .build()
                )
                mFragmentManager = null
            } else {
                SnapshotsApplication.currentUser = it.currentUser!!

                val fragmentProfile = mFragmentManager?.findFragmentByTag(ProfileFragment::class.java.name)
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

    // Configurar la navegación inferior
    private fun setupBottomNav(fragmentManager: FragmentManager) {
        mFragmentManager?.let { // Limpiar antes para prevenir errores
            for (fragment in it.fragments) {
                it.beginTransaction().remove(fragment).commit()
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
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {
                    fragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }

        mBinding.bottomNav.setOnItemReselectedListener {
            when (it.itemId) {
                R.id.action_home -> (homeFragment as HomeAux).goToTop()
            }
        }

        // Set default selected item to home
        mBinding.bottomNav.selectedItemId = R.id.action_home
    }

    // Agregar el listener de autenticación al reanudar la actividad
    override fun onResume() {
        super.onResume()
        mFirebaseAuth?.addAuthStateListener(mAuthListener)
    }

    // Remover el listener de autenticación al pausar la actividad
    override fun onPause() {
        super.onPause()
        mFirebaseAuth?.removeAuthStateListener(mAuthListener)
    }

    // Implementación de la interfaz MainAux
    override fun showMessage(resId: Int, duration: Int) {
        Snackbar.make(mBinding.root, resId, duration)
            .setAnchorView(mBinding.bottomNav)
            .show()
    }
}