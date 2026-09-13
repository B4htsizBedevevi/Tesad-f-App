package com.tesaduf.app

import android.app.Activity
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.LinearGradient
import android.graphics.Shader
import android.graphics.Typeface
import android.view.View
import android.view.animation.OvershootInterpolator
import android.animation.ValueAnimator
import kotlin.math.min
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView

class MainActivity : Activity() {
    private val prefs by lazy { getSharedPreferences("tesaduf", Context.MODE_PRIVATE) }
    private val bg = Color.rgb(7, 9, 20)
    private val card = Color.rgb(17, 20, 38)
    private val white = Color.WHITE
    private val muted = Color.rgb(170, 174, 195)
    private val accent = Color.rgb(105, 91, 255)
    private val accent2 = Color.rgb(42, 190, 255)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = bg
        showSplash()
    }

    private fun showSplash() {
        val splash = TesadufSplashView(this)
        setContentView(splash)
        splash.postDelayed({
            splash.animateExit {
                if (prefs.getString("profile_ready", null) == "yes") showHome() else showProfileSetup()
            }
        }, 2300)
    }

    private inner class TesadufSplashView(context: Context) : View(context) {
        private val cyan = Color.rgb(42, 210, 255)
        private val violet = Color.rgb(112, 80, 255)
        private val pink = Color.rgb(244, 52, 205)
        private val gold = Color.rgb(255, 194, 92)
        private val pathPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE; strokeCap = Paint.Cap.ROUND }
        private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = white; textAlign = Paint.Align.CENTER; typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) }
        private val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = muted; textAlign = Paint.Align.CENTER }
        private var reveal = 0f; private var pulse = 0f; private var title = 0f; private var subtitle = 0f
        init {
            ValueAnimator.ofFloat(0f,1f).apply { duration=1500; interpolator=OvershootInterpolator(.7f); addUpdateListener{reveal=it.animatedValue as Float;invalidate()}; start() }
            ValueAnimator.ofFloat(0f,1f).apply { duration=1800; repeatCount=ValueAnimator.INFINITE; addUpdateListener{pulse=it.animatedValue as Float;invalidate()}; start() }
            postDelayed({title=1f;invalidate()},650); postDelayed({subtitle=1f;invalidate()},1000)
        }
        override fun onDraw(canvas: Canvas) {
            canvas.drawColor(bg); val w=width.toFloat(); val h=height.toFloat(); val cx=w/2f; val scale=min(w,h)/430f; val cy=h*.36f; val r=62f*scale; val drift=kotlin.math.sin(pulse*Math.PI*2).toFloat()*5f*scale
            val nebula=Paint(Paint.ANTI_ALIAS_FLAG); nebula.color=Color.argb((18+18*pulse).toInt(),55,70,255); canvas.drawCircle(cx,cy,(145+25*pulse)*scale,nebula); nebula.color=Color.argb((10+12*pulse).toInt(),255,30,190); canvas.drawCircle(cx+50*scale,cy+30*scale,120*scale,nebula)
            val star=Paint(Paint.ANTI_ALIAS_FLAG); val stars=arrayOf(floatArrayOf(.10f,.14f,2f),floatArrayOf(.82f,.18f,2f),floatArrayOf(.18f,.31f,2f),floatArrayOf(.88f,.38f,2f),floatArrayOf(.08f,.52f,1.5f),floatArrayOf(.92f,.58f,2f)); stars.forEachIndexed{i,s->{star.color=if(i%2==0)cyan else pink;star.alpha=(90+120*(.5+.5*kotlin.math.sin(pulse*7+i))).toInt();canvas.drawCircle(w*s[0],h*s[1],s[2]*scale,star)}}
            pathPaint.strokeWidth=2.5f*scale; pathPaint.color=cyan; canvas.drawLine(w*.13f,h*.12f,w*.22f,h*.045f,pathPaint); pathPaint.color=pink; canvas.drawLine(w*.86f,h*.29f,w*.94f,h*.22f,pathPaint)
            val leftPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply{shader=LinearGradient(cx-r,cy-r,cx+r,cy+r,intArrayOf(Color.rgb(24,135,255),violet),null,Shader.TileMode.CLAMP)}; val rightPaint=Paint(Paint.ANTI_ALIAS_FLAG).apply{shader=LinearGradient(cx+r,cy-r,cx-r,cy+r,intArrayOf(Color.rgb(255,86,193),Color.rgb(164,25,215)),null,Shader.TileMode.CLAMP)}
            canvas.drawOval(RectF(cx-r*1.75f,cy-r+drift,cx+r*.1f,cy+r+drift),leftPaint); canvas.drawOval(RectF(cx-r*.1f,cy-r-drift,cx+r*1.75f,cy+r-drift),rightPaint)
            pathPaint.strokeWidth=8f*scale; pathPaint.shader=LinearGradient(cx-r*2,cy,cx+r*2,cy,intArrayOf(cyan,violet,pink),null,Shader.TileMode.CLAMP); val p1=Path().apply{moveTo(cx-r*1.7f,cy+drift);cubicTo(cx-r,cy-r*1.15f,cx+r,cy+r*1.15f,cx+r*1.7f,cy-drift)}; val p2=Path().apply{moveTo(cx-r*1.7f,cy-drift);cubicTo(cx-r,cy+r*1.15f,cx+r,cy-r*1.15f,cx+r*1.7f,cy+drift)}; canvas.drawPath(p1,pathPaint);canvas.drawPath(p2,pathPaint);pathPaint.shader=null
            drawStar(canvas,cx,cy-r*1.45f,(17f+3f*pulse)*scale); textPaint.textSize=45f*scale;textPaint.alpha=(255*title).toInt();canvas.drawText("Tesadüf",cx,h*.60f,textPaint);subPaint.textSize=15f*scale;subPaint.alpha=(255*subtitle).toInt();canvas.drawText("Her sohbet yeni bir hikâye.",cx,h*.66f,subPaint)
            val barY=h*.76f;pathPaint.style=Paint.Style.STROKE;pathPaint.strokeWidth=3f*scale;pathPaint.color=Color.argb(130,120,70,255);canvas.drawRoundRect(RectF(w*.18f,barY,w*.82f,barY+11*scale),8*scale,8*scale,pathPaint);pathPaint.style=Paint.Style.FILL;pathPaint.shader=LinearGradient(w*.18f,barY,w*.82f,barY,intArrayOf(cyan,violet,pink),null,Shader.TileMode.CLAMP);canvas.drawRoundRect(RectF(w*.185f,barY+2*scale,w*(.185f+.63f*reveal),barY+9*scale),5*scale,5*scale,pathPaint);pathPaint.shader=null;subPaint.textSize=12f*scale;subPaint.alpha=220;canvas.drawText("Yeni insanlarla tanışmaya hazırlanıyorsun...",cx,barY+35*scale,subPaint)
        }
        private fun drawStar(canvas:Canvas,x:Float,y:Float,r:Float){val p=Path();for(i in 0 until 8){val a=Math.toRadians((-90+i*45).toDouble());val rr=if(i%2==0)r else r*.34f;val px=x+kotlin.math.cos(a).toFloat()*rr;val py=y+kotlin.math.sin(a).toFloat()*rr;if(i==0)p.moveTo(px,py)else p.lineTo(px,py)};p.close();Paint(Paint.ANTI_ALIAS_FLAG).also{it.color=gold;canvas.drawPath(p,it)}}
        fun animateExit(onEnd:()->Unit){animate().alpha(0f).setDuration(450).withEndAction(onEnd).start()}
    }

    private fun showProfileSetup() {
        val root=baseRoot();val scroll=ScrollView(this).apply{isFillViewport=true};val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(24),dp(18),dp(24),dp(28))};val logo=AnimatedLogoView(this);content.addView(logo,lp(-1,dp(145),1f,0));content.addView(label("Önce seni tanıyalım",28f,white,Typeface.BOLD));content.addView(label("Kayıt yok. İsim, telefon veya e-posta istemiyoruz. Sana sadece anonim bir kimlik veriyoruz.",15f,muted),lp(-1,-2,1f,12));val idCard=roundedCard();idCard.addView(label("ANONİM KİMLİĞİN",11f,muted,Typeface.BOLD));val id=getOrCreateAnonymousId();idCard.addView(label(id,30f,white,Typeface.BOLD),lp(-1,-2,1f,6));idCard.addView(label("Bu kodla sohbetlerde görünürsün.",13f,muted),lp(-1,-2,1f,2));content.addView(idCard,lp(-1,-2,1f,20));content.addView(label("Avatarını seç",18f,white,Typeface.BOLD),lp(-1,-2,1f,4));content.addView(label("İstersen daha sonra değiştirebilirsin.",13f,muted),lp(-1,-2,1f,10));val avatars=listOf("🌙","⚡","🎧","🐺","🦊","🌌","🎮","🪐");val avatarRow=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER};var selected=prefs.getString("avatar",avatars[0])?:avatars[0];val avatarViews=mutableListOf<TextView>();avatars.forEach{avatar->val v=avatarButton(avatar,avatar==selected);v.tag=avatar;v.setOnClickListener{selected=avatar;avatarViews.forEach{updateAvatarState(it,it.tag==selected)}};avatarViews.add(v);avatarRow.addView(v,lp(0,dp(58),1f,5))};content.addView(avatarRow,lp(-1,-2,1f,18));val continueButton=primaryButton("TESADÜFE BAŞLA");continueButton.setOnClickListener{prefs.edit().putString("anonymous_id",id).putString("avatar",selected).putString("profile_ready","yes").apply();showHome()};content.addView(continueButton,lp(-1,dp(54),1f,16));content.addView(label("Kimlik bilgilerin cihazında anonim olarak saklanır.",12f,muted).apply{gravity=Gravity.CENTER},lp(-1,-2,1f,12));scroll.addView(content);root.addView(scroll,lp(-1,-1,1f,0));setContentView(root)
    }

    private fun showHome(){val root=baseRoot();val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(22),dp(16),dp(22),dp(18))};val logo=AnimatedLogoView(this);content.addView(logo,lp(-1,dp(100),1f,0));val top=LinearLayout(this).apply{gravity=Gravity.CENTER_VERTICAL};top.addView(label(prefs.getString("avatar","🌙")?:"🌙",28f,white),lp(dp(48),dp(48),0,0));val identity=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};identity.addView(label("TESADÜF",20f,white,Typeface.BOLD));identity.addView(label(prefs.getString("anonymous_id","#????????")?:"#????????",13f,muted),lp(-1,-2,1f,2));top.addView(identity,lp(0,-2,1f,12));content.addView(top);content.addView(label("Bugün kimin hikâyesine denk geleceksin?",28f,white,Typeface.BOLD),lp(-1,-2,1f,30));val hero=roundedCard();hero.gravity=Gravity.CENTER;hero.addView(label("✦",46f,accent2,Typeface.BOLD).apply{gravity=Gravity.CENTER});hero.addView(label("Bir tesadüf başlat.",21f,white,Typeface.BOLD).apply{gravity=Gravity.CENTER},lp(-1,-2,1f,8));hero.addView(label("Rastgele biriyle anonim bir sohbet.\n15 dakika. Gerisini tesadüfe bırak.",14f,muted).apply{gravity=Gravity.CENTER},lp(-1,-2,1f,4));val start=primaryButton("TESADÜFÜ BAŞLAT");start.setOnClickListener{startActivity(android.content.Intent(this,MatchActivity::class.java))};hero.addView(start,lp(-1,dp(52),1f,20));content.addView(hero,lp(-1,0,1f,20));val privacy=roundedCard();privacy.addView(label("ANONİMLİK ÖNCELİĞİ",11f,accent2,Typeface.BOLD));privacy.addView(label("Gerçek ad yok • Profil fotoğrafı yok • Telefon yok",14f,white),lp(-1,-2,1f,7));privacy.addView(label("Sadece sohbet. Sadece o an.",13f,muted),lp(-1,-2,1f,2));content.addView(privacy,lp(-1,-2,1f,0));content.addView(label("Ayarlar   •   Engellenenler   •   Güvenlik",12f,muted).apply{gravity=Gravity.CENTER},lp(-1,-2,1f,18));root.addView(content,lp(-1,-1,1f,0));setContentView(root)}

    private inner class AnimatedLogoView(context:Context):View(context){private val lp=Paint(Paint.ANTI_ALIAS_FLAG);private val rp=Paint(Paint.ANTI_ALIAS_FLAG);private val ring=Paint(Paint.ANTI_ALIAS_FLAG).apply{style=Paint.Style.STROKE;strokeCap=Paint.Cap.ROUND};private var phase=0f;init{ValueAnimator.ofFloat(0f,1f).apply{duration=1900;repeatCount=ValueAnimator.INFINITE;addUpdateListener{phase=it.animatedValue as Float;invalidate()};start()}};override fun onDraw(c:Canvas){val cx=width/2f;val cy=height*.48f;val s=min(width,height)/210f;val r=54f*s;val d=kotlin.math.sin(phase*Math.PI*2).toFloat()*5f*s;c.drawCircle(cx,cy,84f*s,Paint(Paint.ANTI_ALIAS_FLAG).apply{color=Color.argb(28,80,100,255)});lp.shader=LinearGradient(cx-r,cy-r,cx+r,cy+r,intArrayOf(Color.rgb(30,126,255),Color.rgb(76,44,220)),null,Shader.TileMode.CLAMP);rp.shader=LinearGradient(cx+r,cy-r,cx-r,cy+r,intArrayOf(Color.rgb(255,74,196),Color.rgb(172,30,217)),null,Shader.TileMode.CLAMP);c.drawOval(RectF(cx-r*1.55f,cy-r+d,cx+r*.25f,cy+r+d),lp);c.drawOval(RectF(cx-r*.25f,cy-r-d,cx+r*1.55f,cy+r-d),rp);ring.strokeWidth=7f*s;ring.shader=LinearGradient(cx-r*1.8f,cy,cx+r*1.8f,cy,intArrayOf(Color.rgb(30,215,255),Color.rgb(142,77,255),Color.rgb(255,84,199)),null,Shader.TileMode.CLAMP);val a=Path().apply{moveTo(cx-r*1.55f,cy+d);cubicTo(cx-r,cy-r,cx+r,cy+r,cx+r*1.55f,cy-d)};val b=Path().apply{moveTo(cx-r*1.55f,cy-d);cubicTo(cx-r,cy+r,cx+r,cy-r,cx+r*1.55f,cy+d)};c.drawPath(a,ring);c.drawPath(b,ring);drawStar(c,cx,cy-r*1.55f,13f*s+phase*2f*s)};private fun drawStar(c:Canvas,x:Float,y:Float,r:Float){val p=Path();for(i in 0 until 8){val a=Math.toRadians((-90+i*45).toDouble());val rr=if(i%2==0)r else r*.34f;val px=x+kotlin.math.cos(a).toFloat()*rr;val py=y+kotlin.math.sin(a).toFloat()*rr;if(i==0)p.moveTo(px,py)else p.lineTo(px,py)};p.close();Paint(Paint.ANTI_ALIAS_FLAG).also{it.color=Color.rgb(255,194,92);c.drawPath(p,it)}}}

    private fun getOrCreateAnonymousId():String{prefs.getString("anonymous_id",null)?.let{return it};val installationId=java.util.UUID.randomUUID().toString().replace("-","");val id="#"+installationId.substring(0,8).uppercase(java.util.Locale.ROOT);prefs.edit().putString("anonymous_id",id).apply();return id}
    private fun baseRoot()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setBackgroundColor(bg)}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun lp(w:Int,h:Int,weight:Float=0f,bottom:Int=0)=LinearLayout.LayoutParams(w,h,weight).apply{bottomMargin=dp(bottom)}
    private fun label(text:String,size:Float,color:Int,typeface:Int=Typeface.NORMAL)=TextView(this).apply{this.text=text;setTextSize(size);setTextColor(color);setTypeface(Typeface.DEFAULT,typeface)}
    private fun roundedCard()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(20),dp(20),dp(20),dp(20));background=GradientDrawable().apply{setColor(card);cornerRadius=dp(26).toFloat()}}
    private fun avatarButton(text:String,selected:Boolean)=TextView(this).apply{this.text=text;textSize=27f;gravity=Gravity.CENTER;setTextColor(white);background=GradientDrawable().apply{setColor(if(selected)Color.rgb(31,35,64)else Color.rgb(13,16,32));cornerRadius=dp(20).toFloat();setStroke(dp(if(selected)2 else 1),if(selected)accent else Color.rgb(25,28,50))}}
    private fun updateAvatarState(v:TextView,selected:Boolean){v.background=GradientDrawable().apply{setColor(if(selected)Color.rgb(31,35,64)else Color.rgb(13,16,32));cornerRadius=dp(20).toFloat();setStroke(dp(if(selected)2 else 1),if(selected)accent else Color.rgb(25,28,50))}}
    private fun primaryButton(text:String)=Button(this).apply{this.text=text;setTextColor(white);textSize=15f;isAllCaps=false;background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(Color.rgb(30,190,255),Color.rgb(105,91,255),Color.rgb(240,55,205))).apply{cornerRadius=dp(26).toFloat()}}
}
