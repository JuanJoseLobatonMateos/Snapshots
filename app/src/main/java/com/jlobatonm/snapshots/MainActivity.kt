package com.jlobatonm.snapshots

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.jlobatonm.snapshots.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity()
{
    
    private lateinit var mBinding: ActivityMainBinding //Se inicializa el binding
    
    private lateinit var mActiveFragment: Fragment //Se inicializa el Fragment activo
    private lateinit var mFragmentManager: FragmentManager //Se inicializa el FragmentManager
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v , insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                v.paddingLeft ,
                systemBars.top ,
                v.paddingRight ,
                v.paddingBottom
            )
            insets
        }
        //Se inicializa el binding
        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)
        
        //Llama a la función de configuración de la barra de navegación inferior
        setupBottomNav()
        
    }
    
    //Función de configuración de la barra de navegación inferior
    private fun setupBottomNav()
    {
        mFragmentManager = supportFragmentManager//
        
        val homeFragment = HomeFragment()
        val addFragment = AddFragment()
        val profileFragment = ProfileFragment()
        
        mActiveFragment = homeFragment//Se inicializa el Fragment activo al Fragment de inicio
        
        
        //Se añaden los Fragmentos al FragmentManager
        with(mFragmentManager.beginTransaction()){
            add(R.id.nav_host_fragment,profileFragment, ProfileFragment::class.java.name).hide(profileFragment)//Se añade el Fragmento de perfil y se oculta
            add(R.id.nav_host_fragment,addFragment, AddFragment::class.java.name).hide(addFragment)//Se añade el Fragmento de añadir y se oculta
            add(R.id.nav_host_fragment, homeFragment, HomeFragment::class.java.name)//Se añade el Fragmento de inicio
            commit()
        }
        
        //Se añade el Listener a la barra de navegación inferior
        mBinding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.action_home -> {//Se cambia el Fragment activo al Fragment de inicio
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(homeFragment).commit()
                    mActiveFragment = homeFragment
                    true
                }
                R.id.action_add -> {//Se cambia el Fragment activo al Fragment de añadir
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(addFragment).commit()
                    mActiveFragment = addFragment
                    true
                }
                R.id.action_profile -> {//Se cambia el Fragment activo al Fragment de perfil
                    mFragmentManager.beginTransaction().hide(mActiveFragment).show(profileFragment).commit()
                    mActiveFragment = profileFragment
                    true
                }
                else -> false
            }
        }
        
        
        
        
        
    }
}
