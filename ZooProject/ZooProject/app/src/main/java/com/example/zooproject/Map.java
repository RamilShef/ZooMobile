package com.example.zooproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.zooproject.clickableareasimage.ClickableArea;
import com.example.zooproject.clickableareasimage.ClickableAreasImage;
import com.example.zooproject.clickableareasimage.OnClickableAreaClickedListener;
import com.example.zooproject.clickableareasimage.Animal;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Map extends AppCompatActivity implements OnClickableAreaClickedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        getWindow().setStatusBarColor(getResources().getColor(R.color.white));
        getWindow().setNavigationBarColor(getResources().getColor(R.color.white));

        ImageView image = findViewById(R.id.mapImageView);

        ClickableAreasImage clickableAreasImage = new ClickableAreasImage(image, this);

        List<ClickableArea> clickableAreas = getClickableAreas();
        clickableAreasImage.setClickableAreas(clickableAreas);
    }

    @Override
    public void onClickableAreaTouched(Object item) {
        if (item instanceof Animal) {
            Integer sectionId = ((Animal) item).getSectionId();
            loadSection(sectionId);
//            String text = ((Animal) item).getName();
//            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        }
    }

    private void loadSection(Integer id) {
        ZooApiService apiService = RetrofitClient.getApiService();
        Call<Section> call = apiService.getSectionById(id);

        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(Call<Section> call, Response<Section> response) {
                Section section = response.body();
                if (response.isSuccessful() && section != null) {

                   openSectionActivity(section);
                } else {
                    Toast.makeText(Map.this, "Ошибка: " + response.code(), Toast.LENGTH_SHORT).show();
                }
                return;
            }

            @Override
            public void onFailure(Call<Section> call, Throwable t) {
                Toast.makeText(Map.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openSectionActivity(Section section) {
        Intent intent = new Intent(this, AnimalSection.class);
        intent.putExtra("sectionName", section.getSectionName());
        intent.putExtra("sectionDescription", section.getDescription());
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    private List<ClickableArea> getClickableAreas() {

        List<ClickableArea> clickableAreas = new ArrayList<>();

        clickableAreas.add(new ClickableArea(309, 517, 100, 100, new Animal(1, "Пингвины")));
        clickableAreas.add(new ClickableArea(453, 531, 100, 100, new Animal(4, "Попугаи")));
        clickableAreas.add(new ClickableArea(689, 561, 100, 100, new Animal(5, "Слоны")));
        clickableAreas.add(new ClickableArea(815, 663, 100, 100, new Animal(6, "Павлины")));
        clickableAreas.add(new ClickableArea(876, 737, 100, 100, new Animal(7, "Бобры")));
        clickableAreas.add(new ClickableArea(585, 699, 100, 100, new Animal(8, "Морские котики")));
        clickableAreas.add(new ClickableArea(606, 769, 100, 100, new Animal(9, "Калифорнийские морские львы")));
        clickableAreas.add(new ClickableArea(544, 903, 100, 100, new Animal(10, "Еноты")));
        clickableAreas.add(new ClickableArea(411, 996, 100, 100, new Animal(14, "Кианги")));
        clickableAreas.add(new ClickableArea(667, 981, 100, 100, new Animal(15, "Овцебыки")));
        clickableAreas.add(new ClickableArea(592, 1051, 100, 100, new Animal(1016, "Волки")));
        clickableAreas.add(new ClickableArea(758, 1119, 100, 100, new Animal(1017, "Антилопы гну")));
        clickableAreas.add(new ClickableArea(676, 1221, 100, 100, new Animal(1018, "Рыси")));
        clickableAreas.add(new ClickableArea(789, 1208, 100, 100, new Animal(1019, "Тапиры")));
        clickableAreas.add(new ClickableArea(567, 1334, 100, 100, new Animal(1020, "Медведи очковые")));
        clickableAreas.add(new ClickableArea(654, 1504, 100, 100, new Animal(1021, "Ягуары")));
        clickableAreas.add(new ClickableArea(830, 1591, 100, 100, new Animal(1022, "Фазаны")));
        clickableAreas.add(new ClickableArea(833, 1463, 100, 100, new Animal(1023, "Гепарды")));
        clickableAreas.add(new ClickableArea(873, 1294, 100, 100, new Animal(1024, "Жирафы")));
        clickableAreas.add(new ClickableArea(1026, 1212, 100, 100, new Animal(1025, "Орлы")));
        clickableAreas.add(new ClickableArea(1142, 908, 100, 100, new Animal(1026, "Ленивцы")));
        clickableAreas.add(new ClickableArea(1193, 996, 100, 100, new Animal(1027, "Летучие мыши")));
        clickableAreas.add(new ClickableArea(1265, 1227, 100, 100, new Animal(1022, "Фазаны")));
        clickableAreas.add(new ClickableArea(1316, 931, 100, 100, new Animal(1028, "Суслики")));
        clickableAreas.add(new ClickableArea(1389, 991, 100, 100, new Animal(1029, "Лисы")));
        clickableAreas.add(new ClickableArea(1385, 1096, 100, 100, new Animal(1030, "Глухари")));
        clickableAreas.add(new ClickableArea(1455, 1163, 100, 100, new Animal(1031, "Журавли")));
        clickableAreas.add(new ClickableArea(1301, 1536, 100, 100, new Animal(1032, "Фламинго")));
        clickableAreas.add(new ClickableArea(1601, 1488, 100, 100, new Animal(1033, "Ламы")));
        clickableAreas.add(new ClickableArea(1777, 907, 100, 100, new Animal(1032, "Фламинго")));
        clickableAreas.add(new ClickableArea(1792, 1065, 100, 100, new Animal(1034, "Выдры")));
        clickableAreas.add(new ClickableArea(1860, 1017, 100, 100, new Animal(1035, "Совы")));
        clickableAreas.add(new ClickableArea(2020, 902, 100, 100, new Animal(1036, "Медведи белые")));
        clickableAreas.add(new ClickableArea(2160, 982, 100, 100, new Animal(1037, "Росомахи")));

        return clickableAreas;
    }

    @Override
    protected void onPause() {
        super.onPause();
        MusicPlayer.pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (MusicPlayer.isMusicOn(this)) {
            MusicPlayer.startMusic(this);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) { // Останавливаем только если активность уничтожается
            MusicPlayer.stopMusic();
        }
    }

    public void onMapClick(View view) {
        Intent intent = new Intent(this, Map.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onSearchClick(View view) {
        Intent intent = new Intent(this, Search.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onFeedbackClick(View view) {
        Intent intent = new Intent(this, Feedback.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }

    public void onSettingsClick(View view) {
        Intent intent = new Intent(this, Settings.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(intent);
        overridePendingTransition(0, 0);
    }
}