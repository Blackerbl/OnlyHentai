@echo off
chcp 65001 >nul
echo ----------------------------------------
git status
echo ----------------------------------------
echo.

echo Versiyon guncelleniyor...
powershell -Command "$file = 'Hentaizm/build.gradle.kts'; if (Test-Path $file) { $content = Get-Content $file -Raw; if ($content -match 'version\s*=\s*(\d+)') { $currentVersion = [int]$matches[1]; $newVersion = $currentVersion + 1; $newContent = $content -replace 'version\s*=\s*\d+', ('version = ' + $newVersion); Set-Content $file $newContent -Encoding UTF8; Write-Host ('Versiyon ' + $currentVersion + ' -> ' + $newVersion + ' olarak guncellendi.'); } else { Write-Warning 'Versiyon tanimi bulunamadi!'; } } else { Write-Error 'build.gradle.kts bulunamadi!'; }"

echo.
set /p commit_msg="Commit mesaji girin (Bos birakilirsa 'Otomatik guncelleme' yapilir): "
if "%commit_msg%"=="" set commit_msg=Otomatik guncelleme

echo.
echo Dosyalar ekleniyor...
git add .

echo.
echo Commit aliniyor: "%commit_msg%"
git commit -m "%commit_msg%"

echo.
echo GitHub'a gonderiliyor...
git push origin master

echo.
if %errorlevel% equ 0 (
    echo [BASARILI] Proje guncellendi ve yuklendi.
) else (
    echo [HATA] Bir sorun olustu. Lutfen yukaridaki hatalari kontrol edin.
)

pause
