package com.tesaduf.app.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant

private val MoodOptions=listOf("😄 Eğlenceli","🧠 Derin","🌙 Gece","🎮 Oyun","🎵 Müzik","🎲 Fark etmez")
private val CardPrompts=listOf(
    "Bir günlüğüne istediğin yerde yaşayabilsen neresi olurdu?",
    "Son zamanlarda seni gerçekten güldüren ne oldu?",
    "Kimsenin bilmediği küçük bir huyun ne?",
    "Şu an bir şarkı seçsen hangisi olurdu?",
    "Bugününü tek kelimeyle anlatsan ne derdin?"
)

@Composable
fun TesadufApp(vm:TesadufViewModel){
    val s by vm.state.collectAsState()
    if(s.loading){Center("TESADÜF");return}
    if(s.error!=null&&s.anonymousId==null){ErrorCenter(s.error!!);return}

    when{
        s.searching||s.matchStatus=="waiting"->Searching(vm)
        s.matchId!=null&&!s.ended->Chat(s,vm)
        s.currentTab=="chats"->Chats(s,vm)
        else->Home(s,vm)
    }
}

@Composable
fun Home(s:TesadufState,vm:TesadufViewModel){
    var slogan by remember{mutableStateOf(HomeSlogans.random())}
    var mood by remember{mutableStateOf(s.selectedMood)}
    LaunchedEffect(Unit){while(true){delay(6500);slogan=HomeSlogans.random()}}

    Scaffold(bottomBar={BottomBar("home",vm)}){
        LazyColumn(
            Modifier.fillMaxSize().padding(it).padding(horizontal=20.dp),
            verticalArrangement=Arrangement.spacedBy(14.dp),
            contentPadding=PaddingValues(top=34.dp,bottom=30.dp)
        ){
            item{
                TesadufMark(Modifier.size(92.dp))
                Spacer(Modifier.height(8.dp))
                Text("TESADÜF",style=MaterialTheme.typography.displaySmall,fontWeight=FontWeight.Bold)
                Spacer(Modifier.height(6.dp))
                Text(slogan,style=MaterialTheme.typography.titleMedium)
            }
            item{
                Card{
                    Column(Modifier.padding(18.dp)){
                        Text("Anonim kimliğin",style=MaterialTheme.typography.labelLarge)
                        Spacer(Modifier.height(4.dp))
                        Text(s.anonymousId?:"#-----",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.Bold)
                        Text("Gerçek kimliğin görünmez.",style=MaterialTheme.typography.bodyMedium)
                    }
                }
            }
            item{
                Text("Bugün nasıl bir sohbet?",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)){
                    MoodOptions.chunked(3).forEach{row->
                        Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()){
                            row.forEach{option->
                                FilterChip(
                                    selected=mood==option,
                                    onClick={mood=if(mood==option)null else option},
                                    label={Text(option)},
                                    modifier=Modifier.weight(1f)
                                )
                            }
                            if(row.size<3)Spacer(Modifier.weight((3-row.size).toFloat()))
                        }
                    }
                }
            }
            item{
                Button(
                    onClick={ { vm.find(mood) },
                    enabled=!s.searching,
                    modifier=Modifier.fillMaxWidth().height(60.dp),
                    shape=RoundedCornerShape(18.dp)
                ){Text(if(s.searching)"Bir tesadüf aranıyor…" else "🎲 TESADÜFÜ BAŞLAT",style=MaterialTheme.typography.titleMedium)}
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick={},enabled=false,modifier=Modifier.fillMaxWidth()){
                    Text("🎙️ Sesli Tesadüf  •  Yakında")
                }
            }
            item{Text("Her eşleşmede yeni bir insan, yeni bir hikâye.",style=MaterialTheme.typography.bodySmall)}
            s.error?.let{err->item{Text(err,color=MaterialTheme.colorScheme.error)}}
        }
    }
}

@Composable
fun Searching(vm:TesadufViewModel){
    var slogan by remember{mutableStateOf(SearchSlogans.random())}
    LaunchedEffect(Unit){while(true){delay(4200);slogan=SearchSlogans.random()}}
    Box(Modifier.fillMaxSize().padding(24.dp),contentAlignment=Alignment.Center){
        Column(horizontalAlignment=Alignment.CenterHorizontally){
            Text("✦",style=MaterialTheme.typography.displaySmall,color=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(18.dp))
            Text("Bir tesadüf aranıyor…",style=MaterialTheme.typography.headlineSmall,fontWeight=FontWeight.SemiBold)
            Spacer(Modifier.height(10.dp))
            Text(slogan,style=MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(26.dp))
            CircularProgressIndicator(color=MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(26.dp))
            OutlinedButton(onClick={vm.leave}){Text("Aramayı iptal et")}
        }
    }
}

@Composable
fun Chats(s:TesadufState,vm:TesadufViewModel){
    LaunchedEffect(Unit){vm.loadChats()}
    Scaffold(
        topBar={TopAppBar(title={Text("Sohbetler")})},
        bottomBar={BottomBar("chats",vm)}
    ){p->
        if(s.chatsLoading){
            Box(Modifier.fillMaxSize().padding(p),contentAlignment=Alignment.Center){CircularProgressIndicator()}
        }else if(s.chats.isEmpty()){
            Box(Modifier.fillMaxSize().padding(p).padding(24.dp),contentAlignment=Alignment.Center){
                Column(horizontalAlignment=Alignment.CenterHorizontally){
                    Text("✨",style=MaterialTheme.typography.displaySmall)
                    Text("Henüz bir sohbetin yok.",style=MaterialTheme.typography.titleMedium)
                    Text("Bir tesadüf başlat ve ilk hikâyeni yaz.")
                }
            }
        }else{
            LazyColumn(Modifier.fillMaxSize().padding(p).padding(14.dp),verticalArrangement=Arrangement.spacedBy(10.dp)){
                items(s.chats,key={it.id}){chat->
                    Card(modifier=if(chat.status in listOf("active","destiny"))Modifier.fillMaxWidth().clickable{vm.openChat(chat)} else Modifier.fillMaxWidth()){
                        Row(Modifier.fillMaxWidth().padding(16.dp),verticalAlignment=Alignment.CenterVertically){
                            Column(Modifier.weight(1f)){
                                Text(chat.partner.anonymous_id?:"#?????",style=MaterialTheme.typography.titleMedium,fontWeight=FontWeight.Bold)
                                Text(
                                    when(chat.status){"destiny"->"✨ Kader";"active"->"💬 Aktif";"expired"->"⌛ Süresi doldu";else->"👋 Sonlandı"},
                                    style=MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(if(chat.status in listOf("active","destiny"))"Aç" else "Geçmiş")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Chat(s:TesadufState,vm:TesadufViewModel){
    var text by remember{mutableStateOf("")}
    var showReport by remember{mutableStateOf(false)}
    var showBlock by remember{mutableStateOf(false)}
    var showCard by remember{mutableStateOf(true)}
    val expires=s.expiresAt
    var remaining by remember(expires,s.destiny){
        mutableLongStateOf(expires?.let{runCatching{Duration.between(Instant.now(),Instant.parse(it)).seconds}.getOrDefault(0L)}?:Long.MAX_VALUE)
    }

    LaunchedEffect(s.matchId,expires,s.destiny){
        while(true){
            if(s.destiny){delay(1000)}
            else{
                remaining=expires?.let{runCatching{Duration.between(Instant.now(),Instant.parse(it)).seconds}.getOrDefault(0L)}?:0L
                if(remaining<=0){vm.showDecision();break}
                delay(1000)
            }
        }
    }

    // Message polling is owned by the ViewModel. Keeping a second UI polling loop
    // here caused duplicate network requests and unnecessary recompositions.

    if(showReport){
        AlertDialog(
            onDismissRequest={showReport=false},
            title={Text("Neden şikâyet ediyorsun?")},
            text={Text("Spam, hakaret, uygunsuz içerik veya başka bir neden seçebilirsin.")},
            confirmButton={Button(onClick={vm.reportCurrent("Uygunsuz davranış");showReport=false}){Text("Şikâyet et")}},
            dismissButton={TextButton(onClick={showReport=false}){Text("İptal")}}
        )
    }
    if(showBlock){
        AlertDialog(
            onDismissRequest={showBlock=false},
            title={Text("Bu kişiyi engelle?")},
            text={Text("Engelledikten sonra bu kişiyle tekrar eşleşmezsin ve mevcut sohbet sona erer.")},
            confirmButton={Button(onClick={vm.blockCurrent}){Text("Engelle")}},
            dismissButton={TextButton(onClick={showBlock=false}){Text("Vazgeç")}}
        )
    }
    if(s.decisionVisible){
        AlertDialog(
            onDismissRequest={},
            title={Text("Bu tesadüf hoşuna gitti mi? ✨")},
            text={Text("İkiniz de devam etmeyi seçerseniz tesadüfünüz kadere dönüşür.")},
            confirmButton={Button(onClick={ vm.decide(true) }){Text("💚 Tesadüfü Sürdür")}},
            dismissButton={TextButton(onClick={ vm.decide(false) }){Text("Burada Bitsin")}}
        )
    }

    Scaffold(
        topBar={
            TopAppBar(
                title={Text(s.partnerId?.let{"✦ $it"}?:"Tesadüf")},
                actions={
                    TextButton(onClick={ {showReport=true} }){Text("Şikâyet")}
                    TextButton(onClick={ {showBlock=true} }){Text("Engelle")}
                    TextButton(onClick={ {vm.leave()} }){Text("Bitir")}
                }
            )
        },
        bottomBar={
            Row(Modifier.fillMaxWidth().padding(10.dp),verticalAlignment=Alignment.CenterVertically){
                OutlinedTextField(
                    value=text,
                    onValueChange={if(it.length<=2000)text=it},
                    modifier=Modifier.weight(1f),
                    placeholder={Text("Bir şey söyle…")},
                    maxLines=4
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick={val t=text;text="";vm.send(t)},enabled=text.isNotBlank()){Text("Gönder")}
            }
        }
    ){p->
        Column(Modifier.fillMaxSize().padding(p)){
            Card(Modifier.fillMaxWidth().padding(horizontal=10.dp,vertical=8.dp)){
                Column(Modifier.padding(14.dp)){
                    if(!s.destiny){
                        val mins=remaining.coerceAtLeast(0)/60
                        val secs=remaining.coerceAtLeast(0)%60
                        Text("⏱️ Tesadüfün kalan süresi %02d:%02d".format(mins,secs),fontWeight=FontWeight.SemiBold)
                    }else{
                        Text("✨ KADER — artık süreniz yok",fontWeight=FontWeight.SemiBold,color=MaterialTheme.colorScheme.primary)
                    }
                    if(s.waitingForOther){
                        Spacer(Modifier.height(8.dp))
                        Text("⏳ Karşı tarafın kararını bekliyoruz…",color=MaterialTheme.colorScheme.primary)
                    }
                    if(showCard){
                        Spacer(Modifier.height(10.dp))
                        Text("🃏 Tesadüf Kartı",style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.primary)
                        Text(CardPrompts[(s.messages.size/3)%CardPrompts.size],style=MaterialTheme.typography.bodyMedium)
                        TextButton(onClick={showCard=false}){Text("Kartı gizle")}
                    }
                }
            }
            LazyColumn(
                Modifier.weight(1f).padding(horizontal=10.dp),
                contentPadding=PaddingValues(bottom=8.dp),
                verticalArrangement=Arrangement.spacedBy(6.dp)
            ){
                items(s.messages,key={it.id}){m->
                    val mine=m.sender_id==com.tesaduf.app.data.Supabase.client.auth.currentSessionOrNull()?.user?.id
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=if(mine)Arrangement.End else Arrangement.Start){
                        Surface(
                            shape=RoundedCornerShape(16.dp),
                            tonalElevation=2.dp,
                            modifier=Modifier.widthIn(max=300.dp)
                        ){Text(m.body,Modifier.padding(horizontal=14.dp,vertical=10.dp))}
                    }
                }
            }
            s.error?.let{Text(it,color=MaterialTheme.colorScheme.error,modifier=Modifier.padding(horizontal=12.dp))}
        }
    }
}

@Composable
private fun BottomBar(selected:String,vm:TesadufViewModel){
    NavigationBar{
        NavigationBarItem(selected=selected=="home",onClick={vm.tab("home")},icon={Text("🎲")},label={Text("Tesadüf")})
        NavigationBarItem(selected=selected=="chats",onClick={vm.tab("chats")},icon={Text("💬")},label={Text("Sohbetler")})
    }
}

private val HomeSlogans=listOf(
    "Bazen en iyi sohbetler planlanmaz.",
    "İyi sohbetler tesadüfen başlar.",
    "Karşına kim çıkacak?",
    "Sadece yaz. Gerisini tesadüfe bırak.",
    "Bugün kimin hikâyesine denk geleceksin?",
    "Bir yabancı. Bir sohbet.",
    "Tesadüfen karşılaş. Gerçekten konuş.",
    "Yollar kesişir, sohbet başlar.",
    "Kiminle karşılaşacağını bilemezsin.",
    "Her karşılaşmanın bir hikâyesi vardır.",
    "Tanımadığın biri, bilmediğin bir hikâye.",
    "Belki de bugün birine denk gelirsin.",
    "Tesadüf seni nereye götürür?"
)

private val SearchSlogans=listOf(
    "Bakalım bugün kime denk geleceksin…",
    "Bir yabancı aranıyor…",
    "Belki de aradığın sohbet birazdan karşında.",
    "Yollarınız kesişmek üzere…",
    "Bugünün tesadüfü hazırlanıyor…",
    "Kaderin değil, tesadüfün peşindeyiz.",
    "Bir yerlerde biri de seni bekliyor.",
    "Tesadüf için doğru anı kolluyoruz."
)

private val MatchSlogans=listOf(
    "İşte o an. Bir tesadüf gerçekleşti.",
    "Yollarınız kesişti. Şimdi sıra sohbette.",
    "Karşında yepyeni bir hikâye var.",
    "Bugünün tesadüfü başladı.",
    "Bakalım bu sohbet nereye gidecek?",
    "Tanımadığın biriyle tanışmanın en güzel yolu: bir merhaba."
)

private val DestinySlogans=listOf(
    "Tesadüf yerini kadere bıraktı. ✨",
    "Bugünün tesadüfü, yarının hikâyesi olabilir.",
    "İki taraf da seçti. Sohbetiniz artık size ait.",
    "Bazı tesadüfler biraz daha uzun sürmeli.",
    "Belki de bu karşılaşma tesadüf değildi."
)

@Composable
private fun TesadufMark(modifier:Modifier=Modifier){
    Canvas(modifier){
        val c=Offset(size.width/2,size.height/2)
        val r=size.minDimension*0.39f
        drawCircle(
            brush=Brush.sweepGradient(listOf(Color(0xFF2FB8FF),Color(0xFF704CFF),Color(0xFFFF42B7),Color(0xFF2FB8FF))),
            radius=r,center=c,style=Stroke(width=size.minDimension*0.035f)
        )
        val rect=androidx.compose.ui.geometry.Rect(size.width*0.12f,size.height*0.25f,size.width*0.88f,size.height*0.75f)
        drawArc(Brush.linearGradient(listOf(Color(0xFF35B9FF),Color(0xFF7A55FF))),205f,150f,false,rect.topLeft,rect.size,style=Stroke(width=size.minDimension*0.07f))
        drawArc(Brush.linearGradient(listOf(Color(0xFFFF42B8),Color(0xFFFFC56F))),25f,150f,false,rect.topLeft,rect.size,style=Stroke(width=size.minDimension*0.07f))
        drawCircle(Color(0xFFFFE7AC),radius=size.minDimension*0.055f,center=c)
    }
}

@Composable fun Center(t:String){Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){Text(t,style=MaterialTheme.typography.headlineMedium)}}
@Composable fun ErrorCenter(t:String){Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text("Bir şeyler ters gitti",style=MaterialTheme.typography.headlineSmall);Spacer(Modifier.height(12.dp));Text(t)}}
