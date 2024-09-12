package com.example.mycompose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.layout.ModifierLocalBeyondBoundsLayout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.mycompose.navigation.Screens
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sin

@Composable
fun Register(navController: NavController){
    val email=remember { mutableStateOf("") }
    val password=remember { mutableStateOf("") }
    val confirmPassword=remember { mutableStateOf("") }
    val passwordVisible=remember { mutableStateOf(false) }
    val confirmPasswordVisible= remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(),contentAlignment=Alignment.Center) {
        Column (
            modifier= Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                .padding(10.dp)
                //.background(Color.White)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ){
            Text(
                text = "Register",
                style = TextStyle(fontWeight= FontWeight.Bold),
                fontSize = 40.sp
            )
            Spacer(modifier = Modifier.padding(20.dp))
            Column (horizontalAlignment = Alignment.CenterHorizontally){
                OutlinedTextField(value = email.value,
                    onValueChange ={email.value=it},
                    label = { Text(text = "Email adress")},
                    placeholder = { Text(text = "Email adress")},
                    singleLine=true,
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                OutlinedTextField(
                    value =password.value ,
                    onValueChange = { password.value=it},
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                            Icon(
                                imageVector = if (passwordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                                contentDescription = "Password visibility",
                            )
                        }
                    },
                    label = { Text(text = "Password")},
                    placeholder = { Text(text = "Password")},
                    singleLine = true,
                    visualTransformation = if (passwordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                OutlinedTextField(
                    value = confirmPassword.value,
                    onValueChange ={confirmPassword.value=it},
                    trailingIcon = {
                        IconButton(onClick = {
                            confirmPasswordVisible.value=!confirmPasswordVisible.value
                        })
                        {
                            Icon(
                                imageVector =if (confirmPasswordVisible.value) Icons.Filled.Visibility else Icons.Filled.VisibilityOff ,
                                contentDescription = "Password visibility"
                            )
                        }
                    },
                    label = { Text(text = "Confirm Password")},
                    placeholder = { Text(text = "Confirm Password")},
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible.value) VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(0.8f)
                )

                Spacer(modifier = Modifier.padding(10.dp))
                val controller= LocalContext.current
                Button(
                    onClick = {
                        if (password.value==confirmPassword.value){
                            //process create email and password
                            FirebaseAuth.getInstance().createUserWithEmailAndPassword(
                                email.value,
                                password.value
                            ).addOnSuccessListener {
                                val userId= FirebaseAuth.getInstance().currentUser?.uid
                                val user= mutableMapOf<String,Any>()
                                user["first_name"]="";
                                user["last_name"]="";
                                user["profile_picture"]="";

                                FirebaseFirestore.getInstance().collection("users").document(userId!!)
                                    .set(user)
                                    .addOnSuccessListener {  }
                                    .addOnFailureListener{}

                                navController.navigate(Screens.Profile.name)
                            }
                                .addOnFailureListener {
                                    ///TODO: add codes to handle failure scenario
                                }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(50.dp),
                ) {
                    Text(
                        text ="Register" )
                }

                Spacer(modifier = Modifier.padding(20.dp))
                Row {
                    Text(text = "Already have an account ?",
                        )
                    Text(
                        text ="Login",
                        modifier = Modifier.clickable (onClick = {
                            navController.navigate(Screens.Login.name)
                        }),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.padding(20.dp))
            }
        }

    }

}

@Preview(showBackground = true)
@Composable
fun PreviewRegister(navController: NavController= rememberNavController()){
    Register(navController = navController)
}