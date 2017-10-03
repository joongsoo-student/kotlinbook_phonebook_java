package kr.devdogs.kotlinbook.phonebookjava.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.InputStream;

import io.realm.Realm;
import kr.devdogs.kotlinbook.phonebookjava.R;
import kr.devdogs.kotlinbook.phonebookjava.model.PhoneBook;
import kr.devdogs.kotlinbook.phonebookjava.utils.BitmapUtils;

public class FormActivity extends AppCompatActivity {
    // Intent로 넘어온 Mode 구분 상수
    public static final int MODE_INSERT = 0;
    public static final int MODE_UPDATE = 1;

    // Activity 리턴시 구분 상수
    private static final int REQ_TAKE_PICTURE = 100;
    private static final int REQ_PICK_GALARY = 200;

    // Dialog 선택 상수
    private static final int SELECT_TAKE_PICTURE = 0;
    private static final int SELECT_PICK_GALARY = 1;

    // View
    private Button submitBtn;
    private ImageView photoView;
    private EditText nameView;
    private EditText phoneView;
    private EditText emailView;
    private LinearLayout actionView;
    private Button callBtn;
    private Button smsBtn;
    private Button deleteBtn;
    private String photoPath;

    // 멥버변수
    private Realm realm;
    private int currentMode;
    private PhoneBook currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_form);
        Realm.init(getApplicationContext());
        realm = Realm.getDefaultInstance();

        initView();
        setMode();
        permissionCheck();
    }

    private void initView() {
        photoView = (ImageView) findViewById(R.id.form_photo);
        nameView = (EditText) findViewById(R.id.form_name);
        phoneView = (EditText) findViewById(R.id.form_phone);
        emailView = (EditText) findViewById(R.id.form_email);
        submitBtn = (Button) findViewById(R.id.form_submit);
        actionView = (LinearLayout) findViewById(R.id.form_action_layout);
        deleteBtn = (Button) findViewById(R.id.form_delete);
        callBtn = (Button) findViewById(R.id.form_action_call);
        smsBtn = (Button) findViewById(R.id.form_action_sms);
        setEventListener();
    }

    private void setMode() {
        Intent receiveData = getIntent();
        currentMode = receiveData.getIntExtra("mode", MODE_INSERT);

        if(currentMode == MODE_UPDATE) {
            int phoneId = receiveData.getIntExtra("bookId", -1);

            if(phoneId == -1) {
                Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT);
                finish();
                return;
            }

            currentBook = realm.where(PhoneBook.class).equalTo("id", phoneId).findFirst();
            nameView.setText(currentBook.getName());
            phoneView.setText(currentBook.getPhone());
            emailView.setText(currentBook.getEmail());
            photoView.setImageBitmap(BitmapFactory.decodeFile(currentBook.getPhotoSrc()));

            actionView.setVisibility(View.VISIBLE);
            deleteBtn.setVisibility(View.VISIBLE);
        }
    }


    private void setEventListener() {
        photoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getPhotoImage();
            }
        });

        submitBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String name = nameView.getText().toString();
                final String phone = phoneView.getText().toString();
                final String email = emailView.getText().toString();

                if("".equals(name)
                        || "".equals(phone)) {
                    Toast.makeText(FormActivity.this,
                            "이름, 휴대폰은 필수입니다",
                            Toast.LENGTH_SHORT);
                    return;
                }

                realm.executeTransaction(new Realm.Transaction() {
                     @Override
                     public void execute(Realm realm) {
                         if(currentMode == MODE_INSERT) {
                             currentBook = new PhoneBook();
                             Number currentIdNum = realm.where(PhoneBook.class).max("id");
                             int nextId = currentIdNum == null ? 1 : currentIdNum.intValue() + 1;
                             currentBook.setId(nextId);
                         }

                         currentBook.setName(name);
                         currentBook.setPhone(phone);
                         currentBook.setEmail(email);
                         currentBook.setPhotoSrc(photoPath);

                         realm.insertOrUpdate(currentBook);
                     }
                 });

                finish();
            }
        });

        callBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri = Uri.parse("tel:" + currentBook.getPhone());
                Intent intent = new Intent(Intent.ACTION_DIAL, uri);
                startActivity(intent);
            }
        });

        smsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri uri= Uri.parse("smsto:" + currentBook.getPhone());
                Intent intent= new Intent(Intent.ACTION_SENDTO, uri);
                startActivity(intent);
            }
        });

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(FormActivity.this);

                ab.setTitle("정말 삭제하시겠습니까?");
                ab.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                realm.beginTransaction();
                                currentBook.deleteFromRealm();
                                realm.commitTransaction();
                                finish();
                            }
                        }).setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.cancel();
                            }
                        });
                ab.show();
            }
        });
    }

    private void getPhotoImage() {
        final String items[] = { "카메라에서 가져오기", "앨범에서 가져오기" };
        AlertDialog.Builder ab = new AlertDialog.Builder(this);

        ab.setTitle("사진 가져오기");
        ab.setItems(items,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.dismiss();
                        if(whichButton == SELECT_TAKE_PICTURE) {
                            takePicture();
                        } else if(whichButton == SELECT_PICK_GALARY) {
                            getPhotoFromGalary();
                        }
                    }
                }).setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        dialog.cancel();
                    }
                });
        ab.show();
    }


    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQ_TAKE_PICTURE);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {
            if(requestCode == REQ_TAKE_PICTURE) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                saveThumbnail(thumbnail);
            } else if(requestCode == REQ_PICK_GALARY) {
                try {
                    viewImageFromGallary(data);
                } catch (FileNotFoundException e) {
                    Toast.makeText(this, "이미지를 불러오는데 실패했습니다.", Toast.LENGTH_SHORT);
                }
            }
        } else {
            Toast.makeText(this, "사진 찍기에 실패했습니다", Toast.LENGTH_SHORT);
        }
    }

    private void viewImageFromGallary(Intent receiveData) throws FileNotFoundException {
        InputStream is = getContentResolver().openInputStream(receiveData.getData());
        Bitmap photo = BitmapFactory.decodeStream(is);
        photo = Bitmap.createScaledBitmap(photo, 100, 100, true);

        photoPath = BitmapUtils.saveBitmap(photo);
        photoView.setImageBitmap(photo);
    }

    private void saveThumbnail(Bitmap thumbnail) {
        Bitmap dst = Bitmap.createScaledBitmap(thumbnail, 100, 100, true);
        dst = BitmapUtils.rotate(dst, 90);

        photoPath = BitmapUtils.saveBitmap(dst);
        photoView.setImageBitmap(dst);
    }

    private void getPhotoFromGalary() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setData(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), REQ_PICK_GALARY);
    }

    private boolean permissionCheck() {
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
            }

            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }

            permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 100);
            }
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();
    }
}
