package com.example.our_capstone;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;                                                 //파이어 베이스 인스턴스 선언
    private static final String TAG = "SignUpActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);                              //이 자바가 참조할 페이지, 첫화면으로 지정하는 것은 AndroidManifest.xml에서!!!
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();                                     //파이어베이스 인스턴스 초기화

        findViewById(R.id.sign_up_btn).setOnClickListener(onClickListener);     // xml에서 sign_up_btn이라는 id의 위젯 가져오고 이 친구의 클릭리스너는 밑의 onClickListener이다!!!
        findViewById(R.id.goto_login_btn).setOnClickListener(onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
    }

    View.OnClickListener onClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View v){
             switch (v.getId()){
                 case R.id.sign_up_btn:                                         //클릭을 sign_up_btn 누를때
                     signUp();
                     break;
                 case R.id.goto_login_btn:
                     gotoSignInActivity();
                     break;
             }
        }
    };                                                                          //왜 세미콜론이 들어가는지 모르겠음...

    private void signUp(){
        //알아서 중복되는 id는 못올라가고 에러나게 파이어베이스가 막드라!!!
        final String email = ((EditText)findViewById(R.id.sign_up_id)).getText().toString();          //email이라는 String에 입력한 값 받아와서 string으로 넣어주기
        String password = ((EditText)findViewById(R.id.sign_up_pw)).getText().toString();
        String password2 = ((EditText)findViewById(R.id.sign_up_pw2)).getText().toString();
        final String nm = ((EditText)findViewById(R.id.sign_up_nm)).getText().toString();
        final String bitrh = ((EditText)findViewById(R.id.sign_up_birth)).getText().toString();
        if(email.length()>0 && password.length()>0 && password2.length()>0 && nm.length()>0 && bitrh.length()>0){
            if(password.equals(password2) ) {
                if(bitrh.length()==6){
                    mAuth.createUserWithEmailAndPassword(email, password) //회원가입코드
                        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(nm+'_'+bitrh)                                     // 이름 넣기(생년월일 넣을 때가 없어서 여기따가 넣겠습니다.)
                                            //.setPhotoUri(Uri.parse("https://example.com/jane-q-user/profile.jpg"))  //프사 넣기
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Log.d(TAG, "User profile updated.");
                                                    }
                                                }
                                            });
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    Map<String, Object> person = new HashMap<>();
                                    person.put("name",nm);
                                    person.put("birth",bitrh);
                                    person.put("email",email);
                                    db.collection("people").document(user.getUid()).set(person);
                                    showToast("회원가입 성공!");
                                } else {
                                    // If sign in fails, display a message to the user.
                                    showToast(task.getException().toString());
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                }
                            }
                        });//회원가입코드
                }
                else{
                    showToast("생년월일을 6자로 입력해주세요.");
                }
            }
            else{
                showToast("비밀번호가 일치하지 않습니다.");
            }
        }
        else{
            showToast("입력사항들을 입력해주세요.");
        }
    }
    private void showToast(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    private void gotoSignInActivity(){
        Intent intent=new Intent(this,SignInActivity.class);
        startActivity(intent);
    }

}
